package me.proton.pass.test.domain

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId
import me.proton.pass.test.TestUtils.randomString
import me.proton.pass.test.crypto.TestKeyStoreCrypto
import kotlin.random.Random

object TestItem {

    fun create(
        itemType: ItemType = ItemType.Password,
        allowedPackageNames: List<String> = emptyList(),
        keyStoreCrypto: KeyStoreCrypto? = null
    ): Item {
        val title = "item-title"
        val note = "item-note"
        return Item(
            id = ItemId(id = "item-id"),
            revision = 0,
            shareId = ShareId(id = "share-id"),
            itemType = itemType,
            title = keyStoreCrypto?.let { title.encrypt(it) } ?: title,
            note = keyStoreCrypto?.let { note.encrypt(it) } ?: note,
            content = EncryptedByteArray(byteArrayOf()),
            allowedPackageNames = allowedPackageNames
        )
    }

    fun random(
        itemType: ItemType? = null,
        title: String? = null,
        note: String? = null,
        content: ByteArray? = null
    ): Item {
        val itemTypeParam = itemType ?: ItemType.Login(
            randomString(),
            randomString().encrypt(TestKeyStoreCrypto),
            emptyList()
        )
        val titleParam = title ?: randomString()
        val noteParam = note ?: randomString()
        val itemContent = if (content != null) {
            TestKeyStoreCrypto.encrypt(PlainByteArray(content))
        } else {
            TestKeyStoreCrypto.encrypt(PlainByteArray(byteArrayOf(0x00)))
        }
        return Item(
            id = ItemId(randomString()),
            revision = Random.nextLong(),
            shareId = ShareId(randomString()),
            itemType = itemTypeParam,
            title = TestKeyStoreCrypto.encrypt(titleParam),
            note = TestKeyStoreCrypto.encrypt(noteParam),
            content = itemContent,
            allowedPackageNames = emptyList()
        )
    }
}
