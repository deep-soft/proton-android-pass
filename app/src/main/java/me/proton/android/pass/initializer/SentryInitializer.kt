/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.pass.initializer

import android.content.Context
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import io.sentry.android.timber.SentryTimberIntegration
import me.proton.android.pass.appconfig.api.AppConfig
import me.proton.android.pass.appconfig.api.BuildFlavor.Companion.toValue

class SentryInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            SentryInitializerEntryPoint::class.java
        )
        val appConfig = entryPoint.appConfig()

        if (appConfig.sentryDSN?.isNotBlank() == true) {
            SentryAndroid.init(context) { options: SentryOptions ->
                options.dsn = appConfig.sentryDSN
                options.release = appConfig.versionName
                options.environment = appConfig.flavor.toValue()
                if (!appConfig.isDebug) {
                    // This approach doesn't respect core telemetry, a refactor is needed
                    options.addIntegration(
                        SentryTimberIntegration(
                            minEventLevel = SentryLevel.ERROR,
                            minBreadcrumbLevel = SentryLevel.INFO
                        )
                    )
                }
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SentryInitializerEntryPoint {
        fun appConfig(): AppConfig
    }
}
