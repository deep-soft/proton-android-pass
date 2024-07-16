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

package proton.android.pass.composecomponents.impl.item.details.sections.notes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.item.details.modifiers.contentDiff
import proton.android.pass.domain.ItemDiffType
import proton.android.pass.domain.ItemDiffs

@Composable
internal fun PassNoteItemDetailMainSection(
    modifier: Modifier = Modifier,
    note: String,
    itemDiffs: ItemDiffs.Note
) {
    if (note.isNotBlank()) {
        SelectionContainer(modifier = modifier.contentDiff(itemDiffs.note)) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .applyIf(
                        condition = itemDiffs.note == ItemDiffType.Field,
                        ifTrue = { padding(all = Spacing.medium) }
                    ),
                text = note,
                style = ProtonTheme.typography.defaultNorm,
                color = if (itemDiffs.note == ItemDiffType.Content) {
                    PassTheme.colors.signalWarning
                } else {
                    Color.Unspecified
                }
            )
        }
    }
}
