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
                entryPoint = "mainCli"

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
//                runtimeOnly("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.2")

                implementation(libs.kotlinx.cli)
                implementation(libs.kermit)
                implementation(libs.okio)
                implementation(libs.uuid)
                implementation(libs.datetime)
            }
        }

        val macosX64Main by getting {
            dependencies {
                implementation(files("../proc-build/macosX64/main/klib/proc.klib"))
                implementation(files("../engine-build/macosX64/main/klib/engine.klib"))
                implementation(files("macosX64/main/klib/cli.klib"))
            }
        }

        val macosArm64Main by getting {
            dependencies {
                implementation(files("../proc-build/macosArm64/main/klib/proc.klib"))
                implementation(files("../engine-build/macosArm64/main/klib/engine.klib"))
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

