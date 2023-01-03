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

import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("io.sentry.android.gradle")
}

base {
    archivesName.set(Config.archivesBaseName)
}

tasks.register("getArchivesName") {
    doLast {
        println("[ARCHIVES_NAME]${Config.archivesBaseName}")
    }
}

val privateProperties = Properties().apply {
    try {
        load(FileInputStream("private.properties"))
    } catch (exception: java.io.FileNotFoundException) {
        // Provide empty properties to allow the app to be built without secrets
        Properties()
    }
}

val sentryDSN: String? = privateProperties.getProperty("SENTRY_DSN")
val proxyToken: String? = privateProperties.getProperty("PROXY_TOKEN")

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "me.proton.android.pass"

    defaultConfig {
        applicationId = "me.proton.android.pass"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = Config.versionCode
        versionName = Config.versionName
        testInstrumentationRunner = Config.testInstrumentationRunner

        buildConfigField("String", "SENTRY_DSN", sentryDSN.toBuildConfigValue())
        buildConfigField("String", "PROXY_TOKEN", proxyToken.toBuildConfigValue())
        buildConfigField("String", "HUMAN_VERIFICATION_HOST", "verify.proton.me".toBuildConfigValue())
    }

    buildFeatures {
        buildConfig = true
        compose = true
        dataBinding = true // required by Core presentation
    }

    signingConfigs {
        register("release") {
            storeFile = file("$rootDir/keystore/ProtonMail.keystore")
            storePassword = "${privateProperties["keyStorePassword"]}"
            keyAlias = "ProtonMail"
            keyPassword = "${privateProperties["keyStoreKeyPassword"]}"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isTestCoverageEnabled = false
            postprocessing {
                isObfuscate = false
                isOptimizeCode = false
                isRemoveUnusedCode = false
                isRemoveUnusedResources = false
            }
        }
        release {
            isDebuggable = false
            isTestCoverageEnabled = false
            postprocessing {
                isObfuscate = false
                isOptimizeCode = true
                isRemoveUnusedCode = true
                isRemoveUnusedResources = true
                file("proguard").listFiles()?.forEach { proguardFile(it) }
            }
            signingConfig = signingConfigs["release"]
        }
    }

    flavorDimensions += "default"
    productFlavors {
        val gitHash = "git rev-parse --short HEAD".runCommand(workingDir = rootDir)
        create("dev") {
            isDefault = true
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev+$gitHash"
            buildConfigField("Boolean", "USE_DEFAULT_PINS", "false")
            buildConfigField("String", "HOST", "\"proton.black\"")
            buildConfigField("String", "HUMAN_VERIFICATION_HOST", "\"verify.proton.black\"")
            buildConfigField("Boolean", "ALLOW_SCREENSHOTS", "true")
        }
        create("alpha") {
            applicationIdSuffix = ".alpha"
            versionNameSuffix = "-alpha.${Config.versionCode}+$gitHash"
            buildConfigField("Boolean", "USE_DEFAULT_PINS", "true")
            buildConfigField("String", "HOST", "\"protonmail.ch\"")
            buildConfigField("Boolean", "ALLOW_SCREENSHOTS", "true")
        }
        create("prod") {
            buildConfigField("Boolean", "USE_DEFAULT_PINS", "true")
            buildConfigField("String", "HOST", "\"protonmail.ch\"")
            buildConfigField("Boolean", "ALLOW_SCREENSHOTS", "true")
        }
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin", "src/uiTest/kotlin")
        getByName("androidTest").assets.srcDirs("src/uiTest/assets")
        getByName("dev").res.srcDirs("src/dev/res")
        getByName("alpha").res.srcDirs("src/alpha/res")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    hilt {
        enableAggregatingTask = true
    }

    packagingOptions {
        resources.excludes.add("META-INF/licenses/**")
        resources.excludes.add("META-INF/AL2.0")
        resources.excludes.add("META-INF/LGPL2.1")
    }

    kapt {
        correctErrorTypes = true
    }
}

tasks.create("publishGeneratedReleaseNotes") {
    doLast {
        val releaseNotesDir = File("${project.projectDir}/src/main/play/release-notes/en-US")
        releaseNotesDir.mkdirs()
        val releaseNotesFile = File(releaseNotesDir, "default.txt")
        // Limit of 500 chars on Google Play console for release notes
        releaseNotesFile.writeText(
            generateChangelog(
                rootDir,
                since = System.getenv("CI_COMMIT_BEFORE_SHA")
            ).let { changelog ->
                if (changelog.length <= 490) {
                    changelog
                } else {
                    ("${changelog.take(490)}...")
                }
            })
    }
}

tasks.create("printGeneratedChangelog") {
    doLast {
        println(generateChangelog(rootDir, since = System.getProperty("since")))
    }
}

dependencies {
    implementation(files("../../proton-libs/gopenpgp/gopenpgp.aar"))
    implementation(libs.accompanist.navigation.animation)
    implementation(libs.accompanist.insets)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.compose.foundationLayout)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundationLayout)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.core.account)
    implementation(libs.core.accountManager)
    implementation(libs.core.accountManager.presentation.compose)
    implementation(libs.core.auth)
    implementation(libs.core.challenge)
    implementation(libs.core.country)
    implementation(libs.core.crypto)
    implementation(libs.core.cryptoValidator)
    implementation(libs.core.data)
    implementation(libs.core.dataRoom)
    implementation(libs.core.domain)
    implementation(libs.core.eventManager)
    implementation(libs.core.featureFlag)
    implementation(libs.core.humanVerification)
    implementation(libs.core.key)
    implementation(libs.core.payment)
    implementation(libs.core.plan)
    implementation(libs.core.presentation)
    implementation(libs.core.presentation.compose)
    implementation(libs.core.report)
    implementation(libs.core.user)
    implementation(libs.core.userSettings)
    implementation(libs.core.utilAndroidDagger)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.material)
    implementation(libs.okhttp)
    implementation(libs.plumber)

    add("devImplementation", libs.showkase)
    add("kspDev", libs.showkaseProcessor)

    implementation(projects.pass.appConfig.api)
    implementation(projects.pass.autofill.impl)
    implementation(projects.pass.biometry.api)
    implementation(projects.pass.biometry.impl)
    implementation(projects.pass.clipboard.impl)
    implementation(projects.pass.common.api)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.data.api)
    implementation(projects.pass.data.impl)
    implementation(projects.pass.domain)
    implementation(projects.pass.navigation.api)
    implementation(projects.pass.network.api)
    implementation(projects.pass.network.impl)
    implementation(projects.pass.notifications.api)
    implementation(projects.pass.notifications.impl)
    implementation(projects.pass.preferences.api)
    implementation(projects.pass.preferences.impl)
    implementation(projects.pass.presentation)
    implementation(projects.pass.log.api)
    implementation(projects.pass.log.impl)
    implementation(projects.pass.tracing.impl)

    debugImplementation(libs.leakCanary)
    debugImplementation(libs.androidx.compose.uiTooling)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
}

// configureJacoco(flavor = "dev")

fun String?.toBuildConfigValue() = if (this != null) "\"$this\"" else "null"
