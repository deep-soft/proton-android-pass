package me.proton.android.pass.ui.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.navArgument
import me.proton.android.pass.navigation.api.AliasOptionalNavArgId
import me.proton.android.pass.navigation.api.CommonNavArgId
import me.proton.android.pass.navigation.api.NavArgId
import me.proton.android.pass.navigation.api.NavItem
import me.proton.android.pass.navigation.api.OptionalNavArgId
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId

sealed class AppNavItem(
    val baseRoute: String,
    private val navArgIds: List<NavArgId> = emptyList(),
    private val optionalArgIds: List<OptionalNavArgId> = emptyList(),
    override val isTopLevel: Boolean = false
) : NavItem {
    override val route = run {
        buildString {
            val argKeys = navArgIds.map { "{${it.key}}" }
            append(listOf(baseRoute).plus(argKeys).joinToString("/"))
            if (optionalArgIds.isNotEmpty()) {
                val optionalArgKeys = optionalArgIds.joinToString(
                    prefix = "?",
                    separator = "&",
                    transform = { "${it.key}={${it.key}}" }
                )
                append(optionalArgKeys)
            }
        }
    }

    override val args: List<NamedNavArgument> =
        navArgIds.map { navArgument(it.key) { type = it.navType } }
            .plus(
                optionalArgIds.map {
                    navArgument(it.key) {
                        if (it.navType.isNullableAllowed) {
                            nullable = true
                        }
                        if (it.default != null) {
                            defaultValue = it.default
                        }
                        type = it.navType
                    }
                }
            )

    object OnBoarding : AppNavItem("onBoarding")

    object Auth : AppNavItem("auth")

    object Home : AppNavItem("home", isTopLevel = true)

    object Settings : AppNavItem("settings", isTopLevel = true)

    object Trash : AppNavItem("trash", isTopLevel = true)

    object Help : AppNavItem("help", isTopLevel = true)

    object CreateLogin : AppNavItem("createLogin", listOf(CommonNavArgId.ShareId)) {
        fun createNavRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
    }

    object EditLogin : AppNavItem(
        baseRoute = "editLogin",
        navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
    ) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) =
            "$baseRoute/${shareId.id}/${itemId.id}"
    }

    object CreateAlias : AppNavItem(
        baseRoute = "createAlias",
        navArgIds = listOf(CommonNavArgId.ShareId),
        optionalArgIds = listOf(AliasOptionalNavArgId.Title, AliasOptionalNavArgId.IsDraft)
    ) {
        fun createNavRoute(
            shareId: ShareId,
            title: Option<String> = None,
            isDraft: Boolean = false
        ) = buildString {
            append("$baseRoute/${shareId.id}")
            append("?${AliasOptionalNavArgId.IsDraft.key}=$isDraft")
            if (title.isNotEmpty()) append("&${AliasOptionalNavArgId.Title.key}=${title.value()}")
        }
    }

    object EditAlias :
        AppNavItem("editAlias", listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) =
            "$baseRoute/${shareId.id}/${itemId.id}"
    }

    object CreateNote : AppNavItem("createNote", listOf(CommonNavArgId.ShareId)) {
        fun createNavRoute(shareId: ShareId) = "$baseRoute/${shareId.id}"
    }

    object EditNote :
        AppNavItem("editNote", listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) =
            "$baseRoute/${shareId.id}/${itemId.id}"
    }

    object CreatePassword : AppNavItem("createPassword", listOf(CommonNavArgId.ShareId)) {
        fun createNavRoute(shareId: ShareId) = "${CreatePassword.baseRoute}/${shareId.id}"
    }

    object ViewItem :
        AppNavItem("viewItem", listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)) {
        fun createNavRoute(shareId: ShareId, itemId: ItemId) =
            "$baseRoute/${shareId.id}/${itemId.id}"
    }
}
