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

package proton.android.pass.featureonboarding.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.autofill.fakes.TestAutofillManager
import proton.android.pass.biometry.BiometryResult
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.biometry.TestBiometryManager
import proton.android.pass.biometry.TestStoreAuthSuccessful
import proton.android.pass.common.api.None
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.data.fakes.usecases.TestObserveUserAccessData
import proton.android.pass.domain.UserAccessData
import proton.android.pass.featureonboarding.impl.OnBoardingPageName.Autofill
import proton.android.pass.featureonboarding.impl.OnBoardingPageName.Fingerprint
import proton.android.pass.featureonboarding.impl.OnBoardingPageName.InvitePending
import proton.android.pass.featureonboarding.impl.OnBoardingPageName.Last
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.HasCompletedOnBoarding
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.test.MainDispatcherRule

internal class OnBoardingViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: OnBoardingViewModel
    private lateinit var snackbarMessageRepository: TestSnackbarDispatcher
    private lateinit var preferenceRepository: TestPreferenceRepository
    private lateinit var biometryManager: TestBiometryManager
    private lateinit var autofillManager: TestAutofillManager
    private lateinit var observeUserAccessData: TestObserveUserAccessData
    private lateinit var storeAuthSuccessful: TestStoreAuthSuccessful

    @Before
    fun setUp() {
        snackbarMessageRepository = TestSnackbarDispatcher()
        preferenceRepository = TestPreferenceRepository()
        biometryManager = TestBiometryManager()
        autofillManager = TestAutofillManager()
        observeUserAccessData = TestObserveUserAccessData()
        storeAuthSuccessful = TestStoreAuthSuccessful()
    }

    @Test
    fun `given no supported features should show last page`() = runTest {
        observeUserAccessData.sendValue(createUserAccessData())
        biometryManager.setBiometryStatus(BiometryStatus.NotAvailable)
        autofillManager.emitStatus(AutofillSupportedStatus.Unsupported)
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            preferenceRepository.setHasCompletedOnBoarding(HasCompletedOnBoarding.Completed)
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(
                    enabledPages = persistentListOf(Last)
                )
            )
        }
    }

    @Test
    fun `given unsupported autofill should show 1 screen`() = runTest {
        observeUserAccessData.sendValue(createUserAccessData())
        autofillManager.emitStatus(AutofillSupportedStatus.Unsupported)
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(enabledPages = persistentListOf(Fingerprint, Last))
            )
        }
    }

    @Test
    fun `given already enabled autofill should show 1 screen`() = runTest {
        observeUserAccessData.sendValue(createUserAccessData())
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(enabledPages = persistentListOf(Fingerprint, Last))
            )
        }
    }

    @Test
    fun `given a click on enable autofill when fingerprint is available should select next page`() = runTest {
        observeUserAccessData.sendValue(createUserAccessData())
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            skipItems(1)
            viewModel.onMainButtonClick(Autofill, ClassHolder(None))
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(
                    enabledPages = persistentListOf(Autofill, Fingerprint, Last),
                    selectedPage = 1
                )
            )
        }
    }

    @Test
    fun `given a click on enable autofill when fingerprint is not available should select next page`() = runTest {
        observeUserAccessData.sendValue(createUserAccessData())
        biometryManager.setBiometryStatus(BiometryStatus.NotAvailable)
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            skipItems(1)
            viewModel.onMainButtonClick(Autofill, ClassHolder(None))
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(
                    selectedPage = 1,
                    enabledPages = persistentListOf(Autofill, Last)
                )
            )
        }
    }

    @Test
    fun `given a click on skip autofill when fingerprint is available should select next page`() = runTest {
        observeUserAccessData.sendValue(createUserAccessData())
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            skipItems(1)
            viewModel.onSkipButtonClick(Autofill)
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(
                    enabledPages = persistentListOf(Autofill, Fingerprint, Last),
                    selectedPage = 1
                )
            )
        }
    }

    @Test
    fun `given a click on skip autofill when fingerprint is not available should select next page`() = runTest {
        observeUserAccessData.sendValue(createUserAccessData())
        biometryManager.setBiometryStatus(BiometryStatus.NotAvailable)
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            skipItems(1)
            viewModel.onSkipButtonClick(Autofill)
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(
                    selectedPage = 1,
                    enabledPages = persistentListOf(Autofill, Last)
                )
            )
        }
    }

    @Test
    fun `given unsupported biometric should show 1 screen`() = runTest {
        observeUserAccessData.sendValue(createUserAccessData())
        biometryManager.setBiometryStatus(BiometryStatus.NotAvailable)
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(enabledPages = persistentListOf(Autofill, Last))
            )
        }
    }

    @Test
    fun `given a click on enable fingerprint should select last page`() = runTest {
        observeUserAccessData.sendValue(createUserAccessData())
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            skipItems(1)
            biometryManager.emitResult(BiometryResult.Success)
            preferenceRepository.setHasAuthenticated(HasAuthenticated.Authenticated)
            preferenceRepository.setAppLockState(AppLockState.Enabled)
            viewModel.onMainButtonClick(Fingerprint, ClassHolder(None))
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(
                    selectedPage = 1,
                    enabledPages = persistentListOf(Fingerprint, Last)
                )
            )
        }
    }

    @Test
    fun `given a click on skip fingerprint should select last page`() = runTest {
        observeUserAccessData.sendValue(createUserAccessData())
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            skipItems(1)
            viewModel.onSkipButtonClick(Fingerprint)
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(
                    selectedPage = 1,
                    enabledPages = persistentListOf(Fingerprint, Last)
                )
            )
        }
    }

    @Test
    fun `given a click on get started in last page should complete on boarding`() = runTest {
        observeUserAccessData.sendValue(createUserAccessData())
        biometryManager.setBiometryStatus(BiometryStatus.NotAvailable)
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            skipItems(1)
            preferenceRepository.setHasCompletedOnBoarding(HasCompletedOnBoarding.Completed)
            viewModel.onMainButtonClick(Last, ClassHolder(None))
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(
                    enabledPages = persistentListOf(Last),
                    event = OnboardingEvent.OnboardingCompleted
                )
            )
        }
    }

    @Test
    fun `show InvitePending screen when needed`() = runTest {
        val userAccessData = createUserAccessData(waitingNewUserInvites = 1)
        observeUserAccessData.sendValue(userAccessData)
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            val item = awaitItem()
            assertThat(item.enabledPages.contains(InvitePending)).isTrue()
        }
    }

    private fun createViewModel() = OnBoardingViewModel(
        autofillManager = autofillManager,
        biometryManager = biometryManager,
        userPreferencesRepository = preferenceRepository,
        snackbarDispatcher = snackbarMessageRepository,
        observeUserAccessData = observeUserAccessData,
        storeAuthSuccessful = storeAuthSuccessful
    )

    private fun createUserAccessData(
        pendingInvites: Int = 0,
        waitingNewUserInvites: Int = 0,
        needsUpdate: Boolean = false,
        protonMonitorEnabled: Boolean = false,
        aliasMonitorEnabled: Boolean = false,
        minVersionUpgrade: String? = null,
        isSimpleLoginSyncEnabled: Boolean = false,
        simpleLoginSyncPendingAliasCount: Int = 0,
        simpleLoginSyncDefaultShareId: String = "",
        canManageSimpleLoginAliases: Boolean = false
    ) = UserAccessData(
        pendingInvites = pendingInvites,
        waitingNewUserInvites = waitingNewUserInvites,
        needsUpdate = needsUpdate,
        protonMonitorEnabled = protonMonitorEnabled,
        aliasMonitorEnabled = aliasMonitorEnabled,
        minVersionUpgrade = minVersionUpgrade,
        isSimpleLoginSyncEnabled = isSimpleLoginSyncEnabled,
        simpleLoginSyncPendingAliasCount = simpleLoginSyncPendingAliasCount,
        simpleLoginSyncDefaultShareId = simpleLoginSyncDefaultShareId,
        canManageSimpleLoginAliases = canManageSimpleLoginAliases
    )

}
