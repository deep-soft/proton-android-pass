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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.util.kotlin.takeIfNotBlank
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonrust.api.AliasPrefixValidator
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.InvalidContentFormatVersionError
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.ObserveAliasDetails
import proton.android.pass.data.api.usecases.UpdateAlias
import proton.android.pass.data.api.usecases.UpdateAliasContent
import proton.android.pass.data.api.usecases.UpdateAliasItemContent
import proton.android.pass.domain.AliasDetails
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.ItemUpdate
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage.AliasUpdated
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage.InitError
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage.ItemUpdateError
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage.UpdateAppToUpdateItemError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class UpdateAliasViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val updateAliasUseCase: UpdateAlias,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val telemetryManager: TelemetryManager,
    private val aliasPrefixValidator: AliasPrefixValidator,
    private val getItemById: GetItemById,
    private val observeAliasDetails: ObserveAliasDetails,
    savedStateHandleProvider: SavedStateHandleProvider
) : BaseAliasViewModel(snackbarDispatcher, savedStateHandleProvider) {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.w(TAG, throwable)
    }

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private var itemOption: Option<Item> = None

    private var itemDataChanged = false
    private var mailboxesChanged = false
    private var isSLNoteChanged = false

    init {
        viewModelScope.launch(coroutineExceptionHandler) {
            isApplyButtonEnabledState.update { IsButtonEnabled.Disabled }
            setupInitialState()
        }
    }

    internal val updateAliasUiState: StateFlow<UpdateAliasUiState> = combine(
        flowOf(shareId),
        baseAliasUiState,
        selectedMailboxListState
    ) { shareId, aliasUiState, mailboxList ->
        aliasItemFormMutableState = aliasItemFormState.copy(
            mailboxes = aliasItemFormState.mailboxes.map {
                it.copy(selected = mailboxList.contains(it.model.id))
            }
        )

        UpdateAliasUiState(
            selectedShareId = shareId,
            baseAliasUiState = aliasUiState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UpdateAliasUiState.Initial
    )

    override fun onMailboxesChanged(mailboxes: List<SelectedAliasMailboxUiModel>) {
        super.onMailboxesChanged(mailboxes)
        isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
        mailboxesChanged = true
    }

    override fun onNoteChange(value: String) {
        super.onNoteChange(value)
        isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
        itemDataChanged = true
    }

    override fun onSLNoteChange(newSLNote: String) {
        super.onSLNoteChange(newSLNote)
        isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
        isSLNoteChanged = true
    }

    override fun onTitleChange(value: String) {
        onUserEditedContent()
        aliasItemFormMutableState = aliasItemFormState.copy(title = value)
        aliasItemValidationErrorsState.update {
            it.toMutableSet()
                .apply { remove(AliasItemValidationErrors.BlankTitle) }
        }
        isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
        itemDataChanged = true
    }

    private suspend fun setupInitialState() {
        if (itemOption != None) return

        isLoadingState.update { IsLoadingState.Loading }

        runCatching {
            combine(
                oneShot { getItemById(shareId, itemId) },
                observeAliasDetails(shareId, itemId),
                ::Pair
            ).first()
        }.onSuccess { (item, aliasDetails) ->
            itemOption = item.some()
            onAliasDetails(aliasDetails, item)
        }.onFailure { error ->
            showError("Error setting the initial state", InitError, error)
        }

        isLoadingState.update { IsLoadingState.NotLoading }
    }

    private fun onAliasDetails(aliasDetails: AliasDetails, item: Item) {
        AliasDetailsUiModel(aliasDetails).also { details ->
            val alias = item.itemType as ItemType.Alias
            val email = alias.aliasEmail
            val (prefix, suffix) = AliasUtils.extractPrefixSuffix(email)

            val mailboxes = details.availableMailboxes
                .map { mailbox ->
                    SelectedAliasMailboxUiModel(
                        model = mailbox,
                        selected = details.mailboxes.any { it.id == mailbox.id }
                    )
                }
                .toMutableList()
            if (mailboxes.none { it.selected } && mailboxes.isNotEmpty()) {
                val mailbox = mailboxes.removeAt(0)
                mailboxes.add(0, mailbox.copy(selected = true))
                    .also { selectedMailboxListState.update { listOf(mailbox.model.id) } }
            } else {
                selectedMailboxListState.update {
                    mailboxes.filter { it.selected }.map { it.model.id }
                }
            }
            if (aliasItemFormState.title.isNotBlank() || aliasItemFormState.note.isNotBlank()) {
                aliasItemFormMutableState = aliasItemFormState.copy(
                    prefix = prefix,
                    aliasOptions = AliasOptionsUiModel(emptyList(), details.mailboxes),
                    selectedSuffix = AliasSuffixUiModel(suffix, suffix, false, ""),
                    mailboxes = mailboxes,
                    aliasToBeCreated = email,
                    slNote = aliasDetails.slNote.takeIfNotBlank()
                )
            } else {
                aliasItemFormMutableState = encryptionContextProvider.withEncryptionContext {
                    aliasItemFormState.copy(
                        title = decrypt(item.title),
                        note = decrypt(item.note),
                        prefix = prefix,
                        aliasOptions = AliasOptionsUiModel(emptyList(), details.mailboxes),
                        selectedSuffix = AliasSuffixUiModel(suffix, suffix, false, ""),
                        mailboxes = mailboxes,
                        aliasToBeCreated = email,
                        slNote = aliasDetails.slNote.takeIfNotBlank()
                    )
                }
            }
        }
    }

    private suspend fun showError(
        message: String,
        snackbarMessage: AliasSnackbarMessage,
        cause: Throwable? = null
    ) {
        PassLogger.w(TAG, message)
        cause?.let { PassLogger.w(TAG, it) }
        snackbarDispatcher(snackbarMessage)
        mutableCloseScreenEventFlow.update { CloseScreenEvent.Close }
    }

    internal fun updateAlias() {
        viewModelScope.launch(coroutineExceptionHandler) {
            val canUpdate = canUpdateAlias()
            if (!canUpdate) {
                PassLogger.i(TAG, "Cannot update alias")
                return@launch
            }

            val body = createUpdateAliasBody()
            isLoadingState.update { IsLoadingState.Loading }

            val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
            val item = itemOption
            if (userId != null && item is Some) {
                runCatching {
                    updateAliasUseCase(
                        userId = userId,
                        item = item.value,
                        content = body
                    )
                }.onSuccess { newItem ->
                    PassLogger.i(TAG, "Alias successfully updated")
                    isItemSavedState.update {
                        val itemUiModel = encryptionContextProvider.withEncryptionContext {
                            newItem.toUiModel(this)
                        }
                        ItemSavedState.Success(
                            itemId = newItem.id,
                            item = itemUiModel
                        )
                    }
                    isLoadingState.update { IsLoadingState.NotLoading }
                    snackbarDispatcher(AliasUpdated)
                    telemetryManager.sendEvent(ItemUpdate(EventItemType.Alias))
                }.onFailure {
                    PassLogger.w(TAG, "Update alias error")
                    PassLogger.w(TAG, it)
                    val message = if (it is InvalidContentFormatVersionError) {
                        UpdateAppToUpdateItemError
                    } else {
                        ItemUpdateError
                    }
                    snackbarDispatcher(message)
                    isLoadingState.update { IsLoadingState.NotLoading }
                }
            } else {
                PassLogger.i(TAG, "Empty User Id")
                snackbarDispatcher(ItemUpdateError)
                isLoadingState.update { IsLoadingState.NotLoading }
            }
        }
    }

    private fun canUpdateAlias(): Boolean {
        if (!itemDataChanged && !mailboxesChanged && !isSLNoteChanged) {
            PassLogger.i(TAG, "Nor item nor mailboxes have changed")
            return false
        }

        val aliasItemValidationErrors = aliasItemFormState.validate(
            allowEmptyTitle = false,
            aliasPrefixValidator = aliasPrefixValidator
        )
        if (aliasItemValidationErrors.isNotEmpty()) {
            PassLogger.i(TAG, "alias item validation has failed: $aliasItemValidationErrors")
            aliasItemValidationErrorsState.update { aliasItemValidationErrors }
            return false
        }
        return true
    }

    private fun createUpdateAliasBody(): UpdateAliasContent {
        val mailboxes = if (mailboxesChanged) {
            val selectedMailboxes = aliasItemFormState
                .mailboxes
                .filter { it.selected }
                .map { it.model.toDomain() }
            Some(selectedMailboxes)
        } else None

        val itemData = if (itemDataChanged) {
            Some(
                UpdateAliasItemContent(
                    title = aliasItemFormState.title,
                    note = aliasItemFormState.note
                )
            )
        } else None

        return UpdateAliasContent(
            mailboxes = mailboxes,
            itemData = itemData,
            slNoteOption = aliasItemFormState.slNote
                .takeIf { isSLNoteChanged }
                .toOption()
        )
    }

    private companion object {

        private const val TAG = "UpdateAliasViewModel"

    }

}
