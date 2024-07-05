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

package proton.android.pass.commonpresentation.impl.items.details.handlers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.commonui.api.toItemContents
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemState
import javax.inject.Inject

class IdentityItemDetailsHandlerObserverImpl @Inject constructor(
    private val observeVaultById: GetVaultById,
    private val encryptionContextProvider: EncryptionContextProvider
) : ItemDetailsHandlerObserver {

    private val identityItemContentsFlow = MutableStateFlow<ItemContents.Identity?>(null)

    override fun observe(item: Item): Flow<ItemDetailState> = combine(
        observeIdentityItemContents(item),
        observeVaultById(shareId = item.shareId)
    ) { identityItemContents, vault ->
        item.flags
        ItemDetailState.Identity(
            itemContents = identityItemContents,
            itemId = item.id,
            shareId = item.shareId,
            isItemPinned = item.isPinned,
            itemCreatedAt = item.createTime,
            itemModifiedAt = item.modificationTime,
            itemLastAutofillAtOption = item.lastAutofillTime,
            itemRevision = item.revision,
            itemState = ItemState.from(item.state),
            itemVault = vault
        )
    }

    private fun observeIdentityItemContents(item: Item): Flow<ItemContents.Identity> = flow {
        encryptionContextProvider.withEncryptionContext {
            item.toItemContents(this@withEncryptionContext) as ItemContents.Identity
        }.let { identityItemContents ->
            identityItemContentsFlow.update { identityItemContents }
        }.also {
            emitAll(identityItemContentsFlow.filterNotNull())
        }
    }

    override fun updateHiddenState(hiddenFieldType: ItemDetailsFieldType.Hidden, hiddenState: HiddenState) {
        identityItemContentsFlow.update { identityItemContents ->
            when (hiddenFieldType) {
                is ItemDetailsFieldType.Hidden.CustomField,
                ItemDetailsFieldType.Hidden.Cvv,
                ItemDetailsFieldType.Hidden.Password,
                ItemDetailsFieldType.Hidden.Pin -> identityItemContents
            }
        }
    }

}
