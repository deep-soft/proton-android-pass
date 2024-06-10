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

package proton.android.pass.features.secure.links.create.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.component.ProtonDialogTitle
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import proton.android.pass.features.secure.links.R
import proton.android.pass.features.secure.links.create.presentation.SecureLinksCreateState
import proton.android.pass.features.secure.links.create.ui.SecureLinksCreateUiEvent
import proton.android.pass.features.secure.links.create.ui.rows.SecureLinkCreateExpirationOptionRow
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SecureLinkCreateExpirationDialog(
    modifier: Modifier = Modifier,
    selectedExpiration: SecureLinksCreateState.SecureLinkExpiration,
    onUiEvent: (SecureLinksCreateUiEvent) -> Unit
) {
    NoPaddingDialog(
        modifier = modifier.padding(horizontal = Spacing.medium),
        backgroundColor = PassTheme.colors.backgroundStrong,
        onDismissRequest = { onUiEvent(SecureLinksCreateUiEvent.OnExpirationDialogDismissed) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            ProtonDialogTitle(
                modifier = Modifier.padding(
                    start = Spacing.large,
                    top = Spacing.large,
                    bottom = Spacing.medium
                ),
                title = stringResource(id = R.string.secure_links_create_row_expiration_title)
            )

            stringArrayResource(id = R.array.secure_links_create_row_expiration_options)
                .forEachIndexed { index, expirationText ->
                    SecureLinkCreateExpirationOptionRow(
                        text = expirationText,
                        isSelected = index == selectedExpiration.ordinal,
                        onSelected = { onUiEvent(SecureLinksCreateUiEvent.OnExpirationSelected(index)) }
                    )
                }

            TextButton(
                modifier = Modifier
                    .align(alignment = Alignment.End)
                    .padding(
                        end = Spacing.medium,
                        bottom = Spacing.medium
                    ),
                onClick = { onUiEvent(SecureLinksCreateUiEvent.OnExpirationDialogDismissed) }
            ) {
                Text(
                    text = stringResource(id = CompR.string.action_continue),
                    style = ProtonTheme.typography.body2Regular,
                    color = PassTheme.colors.interactionNormMajor2
                )
            }
        }
    }
}

@[Preview Composable]
internal fun SecureLinkCreateExpirationDialogPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SecureLinkCreateExpirationDialog(
                selectedExpiration = SecureLinksCreateState.SecureLinkExpiration.SevenDays,
                onUiEvent = {}
            )
        }
    }
}
