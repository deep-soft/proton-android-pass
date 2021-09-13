package me.proton.android.pass.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.proton.android.pass.db.AppDatabase
import me.proton.core.account.data.db.AccountDatabase
import me.proton.core.humanverification.data.db.HumanVerificationDatabase
import me.proton.core.key.data.db.KeySaltDatabase
import me.proton.core.key.data.db.PublicAddressDatabase
import me.proton.core.pass.data.db.PassDatabase
import me.proton.core.user.data.db.AddressDatabase
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.usersettings.data.db.OrganizationDatabase
import me.proton.core.usersettings.data.db.UserSettingsDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppDatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.buildDatabase(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AppDatabaseBindsModule {
    @Binds
    abstract fun providePassDatabase(db: AppDatabase): PassDatabase

    @Binds
    abstract fun provideAccountDatabase(db: AppDatabase): AccountDatabase

    @Binds
    abstract fun provideUserDatabase(db: AppDatabase): UserDatabase

    @Binds
    abstract fun provideAddressDatabase(db: AppDatabase): AddressDatabase

    @Binds
    abstract fun provideKeySaltDatabase(db: AppDatabase): KeySaltDatabase

    @Binds
    abstract fun providePublicAddressDatabase(db: AppDatabase): PublicAddressDatabase

    @Binds
    abstract fun provideHumanVerificationDatabase(db: AppDatabase): HumanVerificationDatabase

    @Binds
    abstract fun provideUserSettingsDatabase(db: AppDatabase): UserSettingsDatabase

    @Binds
    abstract fun provideOrganizationDatabase(db: AppDatabase): OrganizationDatabase
}
