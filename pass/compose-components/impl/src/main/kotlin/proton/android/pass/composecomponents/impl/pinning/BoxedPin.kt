/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.composecomponents.impl.pinning

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BoxedPin(
    modifier: Modifier = Modifier,
    pinBackgroundColor: Color,
    isShown: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        Box(modifier = Modifier.padding(2.dp)) {
            content()
        }
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomEnd),
            visible = isShown,
            enter = scaleIn(),
            exit = scaleOut()
        ) {
            CircledPin(
                modifier = Modifier
                    .border(
                        width = 2.dp,
                        color = PassTheme.colors.backgroundMedium,
                        shape = CircleShape
                    )
                    .size(24.dp),
                backgroundColor = pinBackgroundColor
            )
        }
    }
}

@Preview
@Composable
fun BoxedPinPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            BoxedPin(
                pinBackgroundColor = PassTheme.colors.loginInteractionNorm,
                isShown = true
            ) {
                LoginIcon(
                    size = 60,
                    shape = PassTheme.shapes.squircleMediumLargeShape,
                    text = "My title",
                    website = null,
                    packageName = null,
                    canLoadExternalImages = false
                )
            }
        }
    }
}