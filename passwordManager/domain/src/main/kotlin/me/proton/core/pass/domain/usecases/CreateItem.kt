package me.proton.core.pass.domain.usecases

import javax.inject.Inject
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemContents
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.domain.repositories.ShareRepository

class CreateItem @Inject constructor(
    private val shareRepository: ShareRepository,
    private val itemRepository: ItemRepository
) {
    suspend operator fun invoke(userId: UserId, shareId: ShareId, itemContents: ItemContents): Item {
        val share = shareRepository.getById(userId, shareId)
        requireNotNull(share)
        return itemRepository.createItem(userId, share, itemContents)
    }
}
