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

package com.duckduckgo.app.browser.omnibar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.duckduckgo.app.browser.R
import com.google.android.material.appbar.AppBarLayout

/*
 * This custom behavior for the bottom omnibar is necessary because the default `HideBottomViewOnScrollBehavior` does not work.
 * The reason is that the `DuckDuckGoWebView` is passing only unconsumed movement, which `HideBottomViewOnScrollBehavior` ignores.
 */
class TopAppBarBehavior(context: Context, attrs: AttributeSet?) : AppBarLayout.Behavior(context, attrs) {
    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int,
    ) {
        if (target.id == R.id.browserWebView) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        }
    }
}
