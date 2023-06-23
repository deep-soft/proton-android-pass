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

package proton.android.pass.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Instant
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestInternalSettingsRepository @Inject constructor() : InternalSettingsRepository {

    private val lastUnlockedTimeFlow = MutableStateFlow<Option<Instant>>(None)
    private val declinedUpdateVersionFlow = MutableStateFlow<String>("")

    override suspend fun setLastUnlockedTime(time: Instant): Result<Unit> {
        lastUnlockedTimeFlow.update { Some(time) }
        return Result.success(Unit)
    }

    override fun getLastUnlockedTime(): Flow<Option<Instant>> = lastUnlockedTimeFlow

    override suspend fun setDeclinedUpdateVersion(versionDeclined: String): Result<Unit> {
        declinedUpdateVersionFlow.update { versionDeclined }
        return Result.success(Unit)
    }

    override fun getDeclinedUpdateVersion(): Flow<String> = declinedUpdateVersionFlow

    override suspend fun clearSettings(): Result<Unit> = Result.success(Unit)
}
