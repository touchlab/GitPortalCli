@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

val GROUP: String by project
val VERSION_NAME: String by project

group = GROUP
version = VERSION_NAME

kotlin {
    listOf(macosX64(), macosArm64()).forEach {
        it.binaries {
            executable {
                entryPoint = "co.touchlab.gitportal.main"

                runTask?.run {
                    val args = providers.gradleProperty("runArgs")
                    args(args.getOrElse("").split(' '))

                    standardOutput = System.out
                    errorOutput = System.err
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.cli)
                implementation(libs.kermit)
                implementation(libs.okio)
                implementation(libs.uuid)
            }
        }

        val macosX64Main by getting {
            dependencies {
                implementation(files("macosX64/main/klib/cli.klib"))
            }
        }

        val macosArm64Main by getting {
            dependencies {
                implementation(files("macosArm64/main/klib/cli.klib"))
            }
        }

        all {
            languageSettings.optIn("kotlinx.cli.ExperimentalCli")
            languageSettings.optIn("kotlinx.cinterop.BetaInteropApi")
            languageSettings.optIn("kotlin.experimental.ExperimentalNativeApi")
        }
    }
}

