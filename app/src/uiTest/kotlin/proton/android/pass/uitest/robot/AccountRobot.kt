/*
 * Copyright (c) 2023 Proton AG.
 * This file is part of Proton Pass.
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

package proton.android.pass.uitest.robot

import me.proton.core.accountmanager.test.robot.AccountSettingsRobot
import me.proton.core.accountmanager.test.robot.SignOutDialogRobot
import me.proton.test.fusion.Fusion.node
import proton.android.pass.features.account.AccountContentTestTag
import proton.android.pass.features.account.AccountScreenTestTag
import proton.android.pass.features.account.R as FeatureAccountR

object AccountRobot : Robot {

    private val accountScreen get() = node.withTag(AccountScreenTestTag.SCREEN)
    private val signOut = node.withText(FeatureAccountR.string.account_sign_out)
    private val upgrade get() = node.withTag(AccountContentTestTag.UPGRADE)
    private val subscription get() = node.withTag(AccountContentTestTag.SUBSCRIPTION)

    fun accountScreenDisplayed(): AccountRobot = apply {
        accountScreen.await { assertIsDisplayed() }
    }

    fun clickSignOut(): SignOutDialogRobot {
        signOut.await { assertIsDisplayed() }
        signOut.scrollTo().click()
        return SignOutDialogRobot
    }

    fun clickUpgrade() {
        upgrade.await { assertIsDisplayed() }
        upgrade.click()
    }

    fun clickSubscription() {
        subscription.await { assertIsDisplayed() }
        subscription.click()
    }

    fun coreAccountSettings(): AccountSettingsRobot = AccountSettingsRobot
}
