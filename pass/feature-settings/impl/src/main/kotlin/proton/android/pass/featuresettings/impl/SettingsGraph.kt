package proton.android.pass.featuresettings.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featuresettings.impl.primaryvault.SelectPrimaryVaultBottomSheet
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

object Settings : NavItem(baseRoute = "settings")
object LogView : NavItem(baseRoute = "log/view")
object ThemeSelector : NavItem(baseRoute = "theme/bottomsheet")
object ClipboardSettings : NavItem(baseRoute = "clipboard/settings/bottomsheet")
object ClearClipboardOptions : NavItem(baseRoute = "clipboard/clearOptions/bottomsheet")
object SelectPrimaryVault : NavItem(baseRoute = "vault/primary/bottomsheet")

@Suppress("LongParameterList")
@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.settingsGraph(
    onSelectThemeClick: () -> Unit,
    onClipboardClick: () -> Unit,
    onClearClipboardSettingClick: () -> Unit,
    onUpClick: () -> Unit,
    onViewLogsClick: () -> Unit,
    onPrimaryVaultClick: () -> Unit,
    dismissBottomSheet: () -> Unit
) {
    composable(Settings) {
        SettingsScreen(
            onSelectThemeClick = onSelectThemeClick,
            onUpClick = onUpClick,
            onViewLogsClick = onViewLogsClick,
            onClipboardClick = onClipboardClick,
            onPrimaryVaultClick = onPrimaryVaultClick
        )
    }

    bottomSheet(ThemeSelector) {
        ThemeSelectionBottomSheet(dismissBottomSheet)
    }

    composable(LogView) {
        LogViewScreen(onUpClick = onUpClick)
    }

    bottomSheet(ClipboardSettings) {
        ClipboardBottomSheet(
            onClearClipboardSettingClick = onClearClipboardSettingClick
        )
    }

    bottomSheet(ClearClipboardOptions) {
        ClearClipboardOptionsBottomSheet(onSuccess = dismissBottomSheet)
    }

    bottomSheet(SelectPrimaryVault) {
        SelectPrimaryVaultBottomSheet(
            onSuccess = dismissBottomSheet,
            onClose = dismissBottomSheet
        )
    }
}
