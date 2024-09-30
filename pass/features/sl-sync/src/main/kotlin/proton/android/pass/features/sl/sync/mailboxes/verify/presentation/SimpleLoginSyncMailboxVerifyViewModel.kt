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

package proton.android.pass.features.sl.sync.mailboxes.verify.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.features.sl.sync.mailboxes.verify.navigation.SimpleLoginSyncMailboxVerifyNavArgId
import javax.inject.Inject

@HiltViewModel
class SimpleLoginSyncMailboxVerifyViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val mailboxId = savedStateHandleProvider.get()
        .require<Long>(SimpleLoginSyncMailboxVerifyNavArgId.key)

    private val verificationCodeFlow = MutableStateFlow(INITIAL_VERIFICATION_CODE)

    private val isLoadingStateFlow: MutableStateFlow<IsLoadingState> = MutableStateFlow(
        value = IsLoadingState.NotLoading
    )

    internal val stateFlow: StateFlow<SimpleLoginSyncMailboxVerifyState> = combine(
        verificationCodeFlow,
        isLoadingStateFlow,
        ::SimpleLoginSyncMailboxVerifyState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SimpleLoginSyncMailboxVerifyState.Initial
    )

    internal fun onVerificationCodeChanged(newVerificationCode: String) {
        verificationCodeFlow.update { newVerificationCode }
    }

    private companion object {

        private const val TAG = "SimpleLoginSyncMailboxVerifyViewModel"

        private const val INITIAL_VERIFICATION_CODE = ""

    }

}
