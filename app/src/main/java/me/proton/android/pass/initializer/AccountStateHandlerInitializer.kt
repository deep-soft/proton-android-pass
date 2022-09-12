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
import me.proton.core.accountmanager.data.AccountStateHandler

class AccountStateHandlerInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AccountStateHandlerInitializerEntryPoint::class.java
        ).accountStateHandler().start()
    }

    override fun dependencies(): List<Class<out Initializer<*>?>> = listOf(
        LoggerInitializer::class.java
    )

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AccountStateHandlerInitializerEntryPoint {
        fun accountStateHandler(): AccountStateHandler
    }
}
