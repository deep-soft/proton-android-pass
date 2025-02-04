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

package proton.android.pass.features.sl.sync.mailboxes.options.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.features.sl.sync.mailboxes.options.presentation.SimpleLoginSyncMailboxOptionsState

@Composable
internal fun SimpleLoginSyncMailboxOptionsContent(
    modifier: Modifier = Modifier,
    state: SimpleLoginSyncMailboxOptionsState,
    onUiEvent: (SimpleLoginSyncMailboxOptionsUiEvent) -> Unit
) = with(state) {
    buildList {
        if (canSetAsDefault) {
            setAsDefault(
                action = action,
                onClick = {
                    onUiEvent(SimpleLoginSyncMailboxOptionsUiEvent.OnSetAsDefaultClicked)
                }
            ).also(::add)
        }

        if (canVerify) {
            verify(
                action = action,
                onClick = {
                    onUiEvent(SimpleLoginSyncMailboxOptionsUiEvent.OnVerifyClicked)
                }
            ).also(::add)
        }

        if (canDelete) {
            delete(
                action = action,
                onClick = {
                    onUiEvent(SimpleLoginSyncMailboxOptionsUiEvent.OnDeleteClicked)
                }
            ).also(::add)
        }
    }.let { items ->
        BottomSheetItemList(
            modifier = modifier.bottomSheet(),
            items = items.toPersistentList()
        )
    }
}
