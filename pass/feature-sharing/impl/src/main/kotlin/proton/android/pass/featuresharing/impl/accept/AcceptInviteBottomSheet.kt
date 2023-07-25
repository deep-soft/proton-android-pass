/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featuresharing.impl.accept

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.featuresharing.impl.SharingNavigation

@Composable
fun AcceptInviteBottomSheet(
    modifier: Modifier = Modifier,
    onNavigateEvent: (SharingNavigation) -> Unit,
    viewModel: AcceptInviteViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state) {
        is AcceptInviteUiState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .bottomSheet()
            ) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp).align(Alignment.Center))
            }
        }
        is AcceptInviteUiState.Error -> {
            LaunchedEffect(Unit) {
                onNavigateEvent(SharingNavigation.Back)
            }
        }
        is AcceptInviteUiState.Content -> {
            val contentState = state as AcceptInviteUiState.Content
            AcceptInviteContent(
                modifier = modifier
                    .fillMaxWidth()
                    .bottomSheet(),
                state = contentState,
                onConfirm = {
                    viewModel.onConfirm(contentState.invite)
                },
                onReject = {
                    viewModel.onReject(contentState.invite)
                }
            )
        }
    }
}
