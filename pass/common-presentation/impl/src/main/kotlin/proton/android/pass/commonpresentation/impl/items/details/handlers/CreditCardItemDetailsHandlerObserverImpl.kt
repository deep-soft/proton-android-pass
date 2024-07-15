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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldSection
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.commonui.api.toItemContents
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemState
import javax.inject.Inject

class CreditCardItemDetailsHandlerObserverImpl @Inject constructor(
    private val getVaultById: GetVaultById,
    private val encryptionContextProvider: EncryptionContextProvider
) : ItemDetailsHandlerObserver<ItemContents.CreditCard>() {

    override fun observe(item: Item): Flow<ItemDetailState> = combine(
        observeCreditCardItemContents(item),
        getVaultById(shareId = item.shareId)
    ) { creditCardItemContents, vault ->
        ItemDetailState.CreditCard(
            itemContents = creditCardItemContents,
            itemId = item.id,
            shareId = item.shareId,
            isItemPinned = item.isPinned,
            itemVault = vault,
            itemCreatedAt = item.createTime,
            itemModifiedAt = item.modificationTime,
            itemLastAutofillAtOption = item.lastAutofillTime,
            itemRevision = item.revision,
            itemState = ItemState.from(item.state),
            itemDiffs = ItemDiffs.CreditCard()
        )
    }

    private fun observeCreditCardItemContents(item: Item): Flow<ItemContents.CreditCard> = flow {
        encryptionContextProvider.withEncryptionContext {
            item.toItemContents(this@withEncryptionContext) as ItemContents.CreditCard
        }.let { creditCardItemContents ->
            emit(creditCardItemContents)
        }
    }

    override fun updateItemContents(
        itemContents: ItemContents.CreditCard,
        hiddenFieldType: ItemDetailsFieldType.Hidden,
        hiddenFieldSection: ItemDetailsFieldSection,
        hiddenState: HiddenState
    ): ItemContents = when (hiddenFieldType) {
        ItemDetailsFieldType.Hidden.Cvv -> itemContents.copy(
            cvv = hiddenState
        )

        ItemDetailsFieldType.Hidden.Pin -> itemContents.copy(
            pin = hiddenState
        )

        is ItemDetailsFieldType.Hidden.CustomField,
        ItemDetailsFieldType.Hidden.Password -> itemContents
    }

    override fun calculateItemDiffs(
        baseItemDetailState: ItemContents.CreditCard,
        otherItemDetailState: ItemContents.CreditCard
    ): ItemDiffs.CreditCard = ItemDiffs.CreditCard(
        title = calculateItemDiffType(baseItemDetailState.title, otherItemDetailState.title)
    )

}
