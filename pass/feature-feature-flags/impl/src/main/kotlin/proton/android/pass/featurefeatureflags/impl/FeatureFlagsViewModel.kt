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

package proton.android.pass.featurefeatureflags.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.combineN
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class FeatureFlagsViewModel @Inject constructor(
    private val ffRepository: FeatureFlagsPreferencesRepository
) : ViewModel() {

    internal val state: StateFlow<Map<FeatureFlag, Boolean>> =
        combineN(
            ffRepository.get<Boolean>(FeatureFlag.SECURITY_CENTER_V1),
            ffRepository.get<Boolean>(FeatureFlag.IDENTITY_V1),
            ffRepository.get<Boolean>(FeatureFlag.USERNAME_SPLIT),
            ffRepository.get<Boolean>(FeatureFlag.ACCESS_KEY_V1),
            ffRepository.get<Boolean>(FeatureFlag.SECURE_LINK_V1),
            ffRepository.get<Boolean>(FeatureFlag.ACCOUNT_SWITCH_V1),
            ffRepository.get<Boolean>(FeatureFlag.SL_ALIASES_SYNC)
        ) { isSecurityCenterEnabled, isIdentityEnabled, isUsernameSplitEnabled,
            isAccessKeyEnabled, isSecureLinkEnabled, isAccountSwitchEnabled, isSimpleLoginAliasesSyncEnabled ->
            mapOf(
                FeatureFlag.SECURITY_CENTER_V1 to isSecurityCenterEnabled,
                FeatureFlag.IDENTITY_V1 to isIdentityEnabled,
                FeatureFlag.USERNAME_SPLIT to isUsernameSplitEnabled,
                FeatureFlag.ACCESS_KEY_V1 to isAccessKeyEnabled,
                FeatureFlag.SECURE_LINK_V1 to isSecureLinkEnabled,
                FeatureFlag.ACCOUNT_SWITCH_V1 to isAccountSwitchEnabled,
                FeatureFlag.SL_ALIASES_SYNC to isSimpleLoginAliasesSyncEnabled
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyMap()
        )

    internal fun <T> override(featureFlag: FeatureFlag, value: T) {
        ffRepository.set(featureFlag = featureFlag, value = value)
    }

}
