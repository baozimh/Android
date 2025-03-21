package com.duckduckgo.app.browser.webview

import android.webkit.WebResourceRequest
import androidx.core.net.toUri
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.app.pixels.remoteconfig.AndroidBrowserConfigFeature
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.feature.toggles.api.FakeFeatureToggleFactory
import com.duckduckgo.feature.toggles.api.Toggle.State
import com.duckduckgo.malicioussiteprotection.api.MaliciousSiteProtection
import com.duckduckgo.malicioussiteprotection.api.MaliciousSiteProtection.IsMaliciousResult.MALICIOUS
import com.duckduckgo.malicioussiteprotection.api.MaliciousSiteProtection.IsMaliciousResult.WAIT_FOR_CONFIRMATION
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class RealMaliciousSiteBlockerWebViewIntegrationTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val maliciousSiteProtection: MaliciousSiteProtection = mock(MaliciousSiteProtection::class.java)
    private val fakeAndroidBrowserConfigFeature = FakeFeatureToggleFactory.create(AndroidBrowserConfigFeature::class.java)
    private val maliciousUri = "http://malicious.com".toUri()
    private val exampleUri = "http://example.com".toUri()
    private val testee = RealMaliciousSiteBlockerWebViewIntegration(
        maliciousSiteProtection,
        androidBrowserConfigFeature = fakeAndroidBrowserConfigFeature,
        dispatchers = coroutineRule.testDispatcherProvider,
        appCoroutineScope = coroutineRule.testScope,
        isMainProcess = true,
        exemptedUrlsHolder = ExemptedUrlsHolder(),
    )

    @Before
    fun setup() {
        updateFeatureEnabled(true)
    }

    @Test
    fun `shouldOverrideUrlLoading returns false when feature is disabled`() = runTest {
        updateFeatureEnabled(false)

        val result = testee.shouldOverrideUrlLoading(exampleUri, true) {}
        assertFalse(result)
    }

    @Test
    fun `shouldInterceptRequest returns null when feature is disabled`() = runTest {
        val request = mock(WebResourceRequest::class.java)
        whenever(request.url).thenReturn(exampleUri)
        updateFeatureEnabled(false)

        val result = testee.shouldIntercept(request, null) {}
        assertNull(result)
    }

    @Test
    fun `shouldOverrideUrlLoading returns false when url is already processed`() = runTest {
        testee.processedUrls.add(exampleUri.toString())

        val result = testee.shouldOverrideUrlLoading(exampleUri, true) {}
        assertFalse(result)
    }

    @Test
    fun `shouldInterceptRequest returns result when feature is enabled, is malicious, and is mainframe`() = runTest {
        val request = mock(WebResourceRequest::class.java)
        whenever(request.url).thenReturn(maliciousUri)
        whenever(request.isForMainFrame).thenReturn(true)
        whenever(maliciousSiteProtection.isMalicious(any(), any())).thenReturn(MALICIOUS)

        val result = testee.shouldIntercept(request, maliciousUri) {}
        assertNotNull(result)
    }

    @Test
    fun `shouldInterceptRequest returns result when feature is enabled, is malicious, and is iframe`() = runTest {
        val request = mock(WebResourceRequest::class.java)
        whenever(request.url).thenReturn(maliciousUri)
        whenever(request.isForMainFrame).thenReturn(true)
        whenever(request.requestHeaders).thenReturn(mapOf("Sec-Fetch-Dest" to "iframe"))
        whenever(maliciousSiteProtection.isMalicious(any(), any())).thenReturn(MALICIOUS)

        val result = testee.shouldIntercept(request, maliciousUri) {}
        assertNotNull(result)
    }

    @Test
    fun `shouldInterceptRequest returns null when feature is enabled, is malicious, and is not mainframe nor iframe`() = runTest {
        val request = mock(WebResourceRequest::class.java)
        whenever(request.url).thenReturn(maliciousUri)
        whenever(request.isForMainFrame).thenReturn(false)
        whenever(maliciousSiteProtection.isMalicious(any(), any())).thenReturn(MALICIOUS)

        val result = testee.shouldIntercept(request, maliciousUri) {}
        assertNull(result)
    }

    @Test
    fun `shouldOverride returns false when feature is enabled, is malicious, and is not mainframe`() = runTest {
        whenever(maliciousSiteProtection.isMalicious(any(), any())).thenReturn(MALICIOUS)

        val result = testee.shouldOverrideUrlLoading(maliciousUri, false) {}
        assertFalse(result)
    }

    @Test
    fun `shouldOverride returns true when feature is enabled, is malicious, and is mainframe`() = runTest {
        whenever(maliciousSiteProtection.isMalicious(any(), any())).thenReturn(MALICIOUS)

        val result = testee.shouldOverrideUrlLoading(maliciousUri, true) {}
        assertTrue(result)
    }

    @Test
    fun `shouldOverride returns false when feature is enabled, is malicious, and not mainframe nor iframe`() = runTest {
        whenever(maliciousSiteProtection.isMalicious(any(), any())).thenReturn(MALICIOUS)

        val result = testee.shouldOverrideUrlLoading(maliciousUri, false) {}
        assertFalse(result)
    }

    @Test
    fun `shouldIntercept returns null when feature is enabled, is malicious, and is mainframe but webView has different host`() = runTest {
        whenever(maliciousSiteProtection.isMalicious(any(), any())).thenReturn(MALICIOUS)
        val request = mock(WebResourceRequest::class.java)
        whenever(request.url).thenReturn(maliciousUri)
        whenever(request.isForMainFrame).thenReturn(false)

        val result = testee.shouldIntercept(request, exampleUri) {}
        assertNull(result)
    }

    @Test
    fun `onPageLoadStarted clears processedUrls`() = runTest {
        testee.processedUrls.add(exampleUri.toString())
        testee.onPageLoadStarted()
        assertTrue(testee.processedUrls.isEmpty())
    }

    @Test
    fun `if a new page load triggering is malicious is started, isMalicious callback result should be ignored for the first page`() = runTest {
        val request = mock(WebResourceRequest::class.java)
        whenever(request.url).thenReturn(maliciousUri)
        whenever(request.isForMainFrame).thenReturn(true)

        val callbackChannel = Channel<Unit>()
        val firstCallbackDeferred = CompletableDeferred<Boolean>()
        val secondCallbackDeferred = CompletableDeferred<Boolean>()

        whenever(maliciousSiteProtection.isMalicious(any(), any())).thenAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean) -> Unit>(1)

            launch {
                callbackChannel.receive()
                callback(true)
            }
            WAIT_FOR_CONFIRMATION
        }

        testee.shouldOverrideUrlLoading(maliciousUri, true) { isMalicious ->
            firstCallbackDeferred.complete(isMalicious)
        }

        testee.shouldOverrideUrlLoading(exampleUri, true) { isMalicious ->
            secondCallbackDeferred.complete(isMalicious)
        }

        callbackChannel.send(Unit)
        callbackChannel.send(Unit)

        val firstCallbackResult = firstCallbackDeferred.await()
        val secondCallbackResult = secondCallbackDeferred.await()

        assertEquals(false, firstCallbackResult)
        assertEquals(true, secondCallbackResult)
    }

    @Test
    fun `isMalicious callback result should be processed if no new page loads triggering isMalicious have started`() = runTest {
        val request = mock(WebResourceRequest::class.java)
        whenever(request.url).thenReturn(maliciousUri)
        whenever(request.isForMainFrame).thenReturn(true)

        val callbackChannel = Channel<Unit>()
        val firstCallbackDeferred = CompletableDeferred<Boolean>()
        val secondCallbackDeferred = CompletableDeferred<Boolean>()

        whenever(maliciousSiteProtection.isMalicious(any(), any())).thenAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean) -> Unit>(1)

            launch {
                callbackChannel.receive()
                callback(true)
            }
            WAIT_FOR_CONFIRMATION
        }

        testee.shouldOverrideUrlLoading(maliciousUri, true) { isMalicious ->
            firstCallbackDeferred.complete(isMalicious)
        }

        callbackChannel.send(Unit)

        testee.shouldOverrideUrlLoading(exampleUri, true) { isMalicious ->
            secondCallbackDeferred.complete(isMalicious)
        }

        callbackChannel.send(Unit)

        val firstCallbackResult = firstCallbackDeferred.await()
        val secondCallbackResult = secondCallbackDeferred.await()

        assertEquals(true, firstCallbackResult)
        assertEquals(true, secondCallbackResult)
    }

    private fun updateFeatureEnabled(enabled: Boolean) {
        fakeAndroidBrowserConfigFeature.enableMaliciousSiteProtection().setRawStoredState(State(enabled))
        testee.onPrivacyConfigDownloaded()
    }
}
