package me.proton.pass.autofill.ui.autofill.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.commonuimodels.api.ItemUiModel
import me.proton.android.pass.featurecreateitem.impl.alias.AliasItem
import me.proton.android.pass.featurecreateitem.impl.alias.RESULT_CREATED_DRAFT_ALIAS
import me.proton.android.pass.featurecreateitem.impl.login.CreateLogin
import me.proton.android.pass.featurecreateitem.impl.login.InitialCreateLoginUiState
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.navigation.api.composable
import me.proton.pass.autofill.entities.AutofillAppState
import me.proton.pass.autofill.ui.autofill.AutofillNavItem

@OptIn(
    ExperimentalAnimationApi::class, ExperimentalLifecycleComposeApi::class
)
fun NavGraphBuilder.createLoginGraph(
    appNavigator: AppNavigator,
    state: AutofillAppState,
    onItemCreated: (ItemUiModel) -> Unit
) {
    composable(AutofillNavItem.CreateLogin) {
        val createdDraftAlias by appNavigator.navState<AliasItem>(RESULT_CREATED_DRAFT_ALIAS, null)
            .collectAsStateWithLifecycle()

        val packageName = if (state.webDomain.isEmpty()) {
            state.packageName
        } else {
            null
        }

        CreateLogin(
            initialContents = InitialCreateLoginUiState(
                title = state.title,
                url = state.webDomain.value(),
                aliasItem = createdDraftAlias,
                packageName = packageName
            ),
            onClose = { appNavigator.onBackClick() },
            onSuccess = onItemCreated,
            onCreateAliasClick = { shareId, titleOption ->
                appNavigator.navigate(
                    AutofillNavItem.CreateAlias,
                    AutofillNavItem.CreateAlias.createNavRoute(
                        shareId = shareId,
                        isDraft = true,
                        title = titleOption
                    )
                )
            }
        )
    }
}
