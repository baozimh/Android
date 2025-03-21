/*
 * Copyright (c) 2024 DuckDuckGo
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

package com.duckduckgo.app.trackerdetection.blocklist

import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.privacy.dashboard.api.PrivacyProtectionTogglePlugin
import com.duckduckgo.privacy.dashboard.api.PrivacyToggleOrigin
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject

@ContributesMultibinding(AppScope::class)
class BlockListPrivacyTogglePlugin @Inject constructor(
    private val blockListPixelsPlugin: BlockListPixelsPlugin,
    private val pixel: Pixel,
) : PrivacyProtectionTogglePlugin {

    override suspend fun onToggleOn(origin: PrivacyToggleOrigin) {
        // NOOP
    }

    override suspend fun onToggleOff(origin: PrivacyToggleOrigin) {
        if (origin == PrivacyToggleOrigin.DASHBOARD) {
            blockListPixelsPlugin.getPrivacyToggleUsed()?.getPixelDefinitions()?.forEach {
                pixel.fire(it.pixelName, it.params)
            }
        }
    }
}
