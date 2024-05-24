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

package proton.android.pass.featureitemcreate.impl.identity.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.Vault
import proton.android.pass.featureitemcreate.impl.common.CreateUpdateTopBar
import proton.android.pass.featureitemcreate.impl.identity.navigation.IdentityContentEvent
import proton.android.pass.featureitemcreate.impl.identity.presentation.IdentityItemFormState
import proton.android.pass.featureitemcreate.impl.identity.presentation.IdentityValidationErrors

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateIdentityContent(
    modifier: Modifier = Modifier,
    identityItemFormState: IdentityItemFormState,
    isLoadingState: IsLoadingState,
    topBarActionName: String,
    selectedVault: Option<Vault>,
    shouldShowVaultSelector: Boolean,
    validationErrors: Set<IdentityValidationErrors>,
    onEvent: (IdentityContentEvent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CreateUpdateTopBar(
                text = topBarActionName,
                isLoading = isLoadingState.value(),
                actionColor = PassTheme.colors.interactionNormMajor1,
                iconColor = PassTheme.colors.interactionNormMajor2,
                iconBackgroundColor = PassTheme.colors.interactionNormMinor1,
                selectedVault = selectedVault.value(),
                showVaultSelector = shouldShowVaultSelector,
                onCloseClick = { onEvent(IdentityContentEvent.Up) },
                onActionClick = {
                    when (selectedVault) {
                        None -> return@CreateUpdateTopBar
                        is Some -> onEvent(IdentityContentEvent.Submit(selectedVault.value.shareId))
                    }
                },
                onUpgrade = { },
                onVaultSelectorClick = {
                    when (selectedVault) {
                        None -> return@CreateUpdateTopBar
                        is Some -> onEvent(IdentityContentEvent.OnVaultSelect(selectedVault.value.shareId))
                    }
                }
            )
        }
    ) { padding ->
        IdentityItemForm(
            modifier = Modifier.padding(padding),
            identityItemFormState = identityItemFormState,
            enabled = !isLoadingState.value(),
            validationErrors = validationErrors,
            onEvent = onEvent
        )
    }
}
