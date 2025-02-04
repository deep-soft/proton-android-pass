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

package proton.android.pass.data.impl.usecases

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import proton.android.pass.data.api.usecases.InitialWorkerLauncher
import proton.android.pass.data.impl.work.PeriodicAssetLinkWorker
import proton.android.pass.data.impl.work.UserAccessWorker
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class InitialWorkerLauncherImpl @Inject constructor(
    private val workManager: WorkManager,
    private val eventWorkerManager: EventWorkerManager,
    private val featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository
) : InitialWorkerLauncher {

    override fun start() {
        launchUserAccessWorker()
        val isDAL = runBlocking {
            featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.DIGITAL_ASSET_LINKS).first()
        }
        if (isDAL) {
            launchAssetLinkWorker()
        } else {
            workManager.cancelUniqueWork(PeriodicAssetLinkWorker.WORKER_UNIQUE_NAME)
        }
    }

    private fun launchAssetLinkWorker() {
        workManager.enqueueUniquePeriodicWork(
            PeriodicAssetLinkWorker.WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicAssetLinkWorker.getRequestFor()
        )
    }

    private fun launchUserAccessWorker() {
        val backoffDelaySeconds = eventWorkerManager.getBackoffDelay().inWholeSeconds
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<UserAccessWorker>(1, TimeUnit.DAYS)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    backoffDelaySeconds,
                    TimeUnit.SECONDS
                )
                .setConstraints(constraints)
                .build()
        workManager.enqueueUniquePeriodicWork(
            UserAccessWorker.WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun cancel() {
        PassLogger.i(TAG, "Cancelling UserAccessWorker")
        workManager.cancelUniqueWork(UserAccessWorker.WORKER_UNIQUE_NAME)
        workManager.cancelUniqueWork(PeriodicAssetLinkWorker.WORKER_UNIQUE_NAME)
    }

    companion object {
        private const val TAG = "UserPlanWorkerLauncherImpl"
    }
}
