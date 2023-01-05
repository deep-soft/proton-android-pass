package me.proton.android.pass.ui.create.alias

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.featurecreateitem.impl.alias.CreateAlias
import me.proton.android.pass.featurecreateitem.impl.alias.RESULT_CREATED_DRAFT_ALIAS
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.navigation.api.composable
import me.proton.android.pass.ui.navigation.AppNavItem

@OptIn(
    ExperimentalAnimationApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
fun NavGraphBuilder.createAliasGraph(nav: AppNavigator) {
    composable(AppNavItem.CreateAlias) {
        CreateAlias(
            onClose = { nav.onBackClick() },
            onUpClick = { nav.onBackClick() },
            onAliasCreated = { nav.onBackClick() },
            onAliasDraftCreated = { aliasItem ->
                nav.navigateUpWithResult(RESULT_CREATED_DRAFT_ALIAS, aliasItem)
            }
        )
    }
}
