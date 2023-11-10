/*
 * Copyright (c) 2023 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.mobile.android.macrobenchmark

import android.os.Build
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Benchmark for Browser Tab Startup. Run this benchmark on a physical device
 * from Android Studio to see startup measurements, and system trace recordings
 * for investigating app's performance.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmarkBrowserTab {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    //region AMP tests
    @Test
    fun loadGoogleVoxAmpSite() {
        loadSite("https://www.google.com/amp/s/www.vox.com/platform/amp/identities/22530103/asians-americans-wealth-income-gap-crazy-rich-model-minority")
    }

    @Test
    fun loadGoogleNyTimesAmpSite() {
        loadSite("https://www.google.com/amp/s/www.nytimes.com/2021/09/20/business/jeff-bezos-earth-fund.amp.html")
    }

    @Test
    fun loadGoogleAmpSite() {
        loadSite("https://www.google.fr/amp/s/www.brookings.edu/research/reforming-global-fossil-fuel-subsidies-how-the-united-states-can-restart-international-cooperation/?amp")
    }

    @Test
    fun loadAmpProjectSite() {
        loadSite("https://www-wpxi-com.cdn.ampproject.org/v/s/www.wpxi.com/news/top-stories/pihl-bans-armstrong-student-section-hockey-games-after-vulgar-chants-directed-female-goalie/G2RP5FZA3ZDYRLFSWGDGQH2MY4/?amp_js_v=a6&_gsa=1&outputType=amp&usqp=mq331AQKKAFQArABIIACAw%3D%3D&referrer=https%3A%2F%2Fwww.google.com&_tf=From%20%251%24s&ampshare=https%3A%2F%2Fwww.wpxi.com%2Fnews%2Ftop-stories%2Fpihl-bans-armstrong-student-section-hockey-games-after-vulgar-chants-directed-female-goalie%2FG2RP5FZA3ZDYRLFSWGDGQH2MY4%2F")
    }

    @Test
    fun loadDotAmpSite() {
        loadSite("https://www.bbc.com/news/world-asia-china-19432800.amp")
    }

    @Test
    fun loadAmpDotSite() {
        loadSite("https://amp.theguardian.com/environment/2021/nov/05/british-firm-to-unveil-technology-for-zero-carbon-emission-flights-at-cop26/world-asia-china-19432800.amp")
    }

    @Test
    fun loadQuestionMarkAmpSite() {
        loadSite("https://www.independent.co.uk/news/world/americas/us-politics/fauci-dog-tests-congress-letter-b1944407.html?amp")
    }
    
    //endregion

    //region other tests

    @Test
    fun loadGeeksHubsAcademyWithQueryParams() {
        loadSite("https://geekshubsacademy.com/producto/tech-management-leadership/?utm_source=facebook&utm_medium=socialmedia-publicidad&utm_campaign=tml_2023&hsa_acc=332045313873526&hsa_cam=23850586615090327&hsa_grp=23851051149020327&hsa_ad=23852254380910327&hsa_src=ig&hsa_net=facebook&hsa_ver=3&fbclid=PAAablhGBkv9-h05KZiQsx0FCHf3nsjUqZfU66j_BEmT9gQlCe5TbhOL8IEiI")
    }

    @Test
    fun loadTwitchHttp() {
        loadSite("http://twitch.tv")
    }


    @Test
    fun loadWhatfinger() {
        loadSite("https://www.whatfinger.com/home2/")
    }

    @Test
    fun loadAnsa() {
        loadSite("www.ansa.it")
    }

    @Test
    fun loadTwitter() {
        loadSite("https://twitter.com/")
    }

    @Test
    fun startAndLoadNYTimes() {
        loadSite("https://www.nytimes.com/")
    }
    
    //endregion


    //region tests from previous performance tests

    @Test
    fun loadTwitchMobile() {
        loadSite("https://m.twitch.tv/")
    }

    @Test
    fun loadWeather() {
        loadSite("https://weather.com/")
    }

    @Test
    fun loadAmazon() {
        loadSite("https://www.amazon.com/")
    }

    @Test
    fun loadBBC() {
        loadSite("https://www.bbc.com/")
    }

    @Test
    fun loadEBay() {
        loadSite("https://www.ebay.com/")
    }

    @Test
    fun loadESPN() {
        loadSite("https://www.espn.com/")
    }

    @Test
    fun loadReddit() {
        loadSite("https://www.reddit.com/")
    }

    @Test
    fun loadTripAdvisor() {
        loadSite("https://www.tripadvisor.com/")
    }

    @Test
    fun loadWallmart() {
        loadSite("https://www.walmart.com/")
    }

    @Test
    fun loadWikipedia() {
        loadSite("https://www.wikipedia.org/")
    }
    
    //endregion


    private fun loadSite(site: String) = startupBenchmark(
        setupBlock = {
            launchBrowserTab()
            pressHome()
        },
        measureBlock = {
            startActivityAndWait()

            val selector = UiSelector()
                .className("android.widget.EditText")
                .instance(0)
            device.findObject(selector).text = site
            device.pressEnter()

            device.waitForIdle(TIMEOUT_MS)
        }
    )

    private fun MacrobenchmarkScope.launchBrowserTab() {
        startActivityAndWait()

        // Handle Notifications permissions on Android 13 and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            device.wait(Until.hasObject(By.text("Allow DuckDuckGo to send you notifications?")), TIMEOUT_MS)
            val allowButton = device.findObject(By.text("Allow"))
            allowButton?.run {
                click()
            }
        }

        device.wait(Until.hasObject(By.text("Let's Do it!")), TIMEOUT_MS)
        val daxPrimaryCtaButton = device.findObject(By.res(packageName, "primaryCta"))
        daxPrimaryCtaButton?.run {
            click()
            device.wait(Until.hasObject(By.text("Cancel")), TIMEOUT_MS)
            val cancelButton = device.findObject(By.text("Cancel"))
            cancelButton?.click()
        }

        device.wait(Until.hasObject(By.text("Next,")), TIMEOUT_MS)
    }

    @OptIn(ExperimentalMetricApi::class)
    private fun startupBenchmark(
        startupMode: StartupMode = StartupMode.WARM,
        setupBlock: MacrobenchmarkScope.() -> Unit,
        measureBlock: MacrobenchmarkScope.() -> Unit = { startActivityAndWait() },
    ) = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE_NAME,
        metrics = listOf(
            StartupTimingMetric(),
            FrameTimingMetric(),
            TraceSectionMetric("LOAD_PAGE_START_TO_FINISH"),
        ),
        iterations = ITERATIONS,
        startupMode = startupMode,
        setupBlock = setupBlock,
        measureBlock = measureBlock
    )
}
