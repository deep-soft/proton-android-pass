/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.InfoBanner
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.SimpleNoteSection
import proton.android.pass.composecomponents.impl.form.TitleSection
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun CreateAliasForm(
    modifier: Modifier = Modifier,
    aliasItemFormState: AliasItemFormState,
    isCreateMode: Boolean,
    onTitleRequiredError: Boolean,
    onAliasRequiredError: Boolean,
    onInvalidAliasError: Boolean,
    isEditAllowed: Boolean,
    isLoading: Boolean,
    showUpgrade: Boolean,
    onSuffixClick: () -> Unit,
    onMailboxClick: () -> Unit,
    onEvent: (AliasContentUiEvent) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        AnimatedVisibility(visible = showUpgrade) {
            InfoBanner(
                backgroundColor = PassTheme.colors.aliasInteractionNormMinor1,
                text = stringResource(R.string.create_alias_content_limit_banner)
            )
        }

        TitleSection(
            modifier = Modifier
                .roundedContainerNorm()
                .padding(
                    start = Spacing.medium,
                    top = Spacing.medium,
                    end = Spacing.extraSmall,
                    bottom = Spacing.medium
                ),
            value = aliasItemFormState.title,
            requestFocus = !isLoading,
            onTitleRequiredError = onTitleRequiredError,
            enabled = isEditAllowed,
            isRounded = true,
            onChange = { onEvent(AliasContentUiEvent.OnTitleChange(it)) }
        )

        if (isCreateMode) {
            CreateAliasSection(
                state = aliasItemFormState,
                onChange = { onEvent(AliasContentUiEvent.OnPrefixChange(it)) },
                onSuffixClick = onSuffixClick,
                canEdit = isEditAllowed,
                canSelectSuffix = aliasItemFormState.aliasOptions.suffixes.size > 1,
                onAliasRequiredError = onAliasRequiredError,
                onInvalidAliasError = onInvalidAliasError
            )
        } else {
            DisplayAliasSection(
                state = aliasItemFormState,
                isLoading = isLoading
            )
        }

        MailboxSection(
            isBottomSheet = false,
            mailboxes = aliasItemFormState.mailboxes.toPersistentList(),
            isCreateMode = isCreateMode,
            isEditAllowed = isEditAllowed && aliasItemFormState.mailboxes.size > 1,
            isLoading = isLoading,
            onMailboxClick = onMailboxClick
        )

        SimpleNoteSection(
            value = aliasItemFormState.note,
            enabled = isEditAllowed,
            onChange = { onEvent(AliasContentUiEvent.OnNoteChange(it)) }
        )

        aliasItemFormState.slNote?.let { slNote ->
            SimpleNoteSection(
                label = buildString {
                    append(stringResource(id = CompR.string.field_note_title))
                    append(" ${SpecialCharacters.DOT_SEPARATOR} ")
                    append(stringResource(id = CompR.string.simple_login_brand_name))
                },
                value = slNote,
                enabled = isEditAllowed,
                onChange = { onEvent(AliasContentUiEvent.OnSLNoteChange(it)) }
            )
        }
    }
}
