/*
 * Copyright (c) 2025 DuckDuckGo
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

package com.duckduckgo.contentscopescripts.impl.features.navigatorinterface

import com.duckduckgo.contentscopescripts.api.ContentScopeConfigPlugin
import com.duckduckgo.contentscopescripts.impl.features.navigatorinterface.NavigatorInterfaceFeatureName.NavigatorInterface
import com.duckduckgo.contentscopescripts.impl.features.navigatorinterface.store.NavigatorInterfaceRepository
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject

@ContributesMultibinding(AppScope::class)
class NavigatorInterfaceContentScopeConfigPlugin @Inject constructor(
    private val navigatorInterfaceRepository: NavigatorInterfaceRepository,
) : ContentScopeConfigPlugin {

    override fun config(): String {
        val featureName = NavigatorInterface.value
        val config = navigatorInterfaceRepository.navigatorInterfaceEntity.json
        return "\"$featureName\":$config"
    }

    override fun preferences(): String? {
        return null
    }
}
