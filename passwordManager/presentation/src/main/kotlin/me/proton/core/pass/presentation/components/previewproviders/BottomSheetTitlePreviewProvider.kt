package me.proton.core.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.core.pass.presentation.R
import me.proton.core.pass.presentation.components.common.bottomsheet.BottomSheetTitleButton

class BottomSheetTitlePreviewProvider : PreviewParameterProvider<BottomSheetTitleButton?> {
    override val values: Sequence<BottomSheetTitleButton?> = sequenceOf(
        BottomSheetTitleButton(title = R.string.action_apply, onClick = {}, enabled = true),
        BottomSheetTitleButton(title = R.string.action_apply, onClick = {}, enabled = false),
        null
    )
}
