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

package proton.android.pass.features.sl.sync.mailboxes.verify.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.text.PassTextWithLink
import proton.android.pass.composecomponents.impl.utils.passTimerText
import proton.android.pass.features.sl.sync.R

@Composable
internal fun SimpleLoginSyncMailboxVerifyCodeResend(
    modifier: Modifier = Modifier,
    showResendVerificationCodeTimer: Boolean = true,
    canRequestVerificationCode: Boolean,
    verificationCodeTimerSeconds: Int,
    onResendVerificationCodeClick: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = showResendVerificationCodeTimer,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = stringResource(
                    id = R.string.simple_login_sync_mailbox_verify_code_resend,
                    passTimerText(seconds = verificationCodeTimerSeconds)
                ),
                color = PassTheme.colors.textWeak,
                style = ProtonTheme.typography.captionWeak
            )
        }

        AnimatedVisibility(
            visible = canRequestVerificationCode,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PassTextWithLink(
                modifier = Modifier.padding(horizontal = Spacing.large),
                textAlign = TextAlign.Center,
                textResId = R.string.simple_login_sync_mailbox_verify_code_not_received,
                textStyle = ProtonTheme.typography.captionWeak.copy(
                    color = PassTheme.colors.textWeak
                ),
                linkResId = R.string.simple_login_sync_mailbox_verify_code_request_new,
                linkStyle = ProtonTheme.typography.captionWeak.copy(
                    color = PassTheme.colors.interactionNormMajor1
                ),
                onLinkClick = { onResendVerificationCodeClick() }
            )
        }
    }
}
