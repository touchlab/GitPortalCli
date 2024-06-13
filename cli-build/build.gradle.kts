@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import java.io.ByteArrayOutputStream


plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

val GROUP: String by project
val VERSION_NAME: String by project

group = GROUP
version = VERSION_NAME

@Throws(RuntimeException::class)
fun Project.xcodeInstallPath(): String = ByteArrayOutputStream().use { outputStream ->
    exec {
        standardOutput = outputStream
        workingDir = projectDir
        commandLine("xcode-select", "--print-path")
    }.let { result ->
        when {
            result.exitValue != 0 -> throw RuntimeException("Command xcode-select failed.")
            else -> outputStream.toString()
                .trim()
        }
    }
}

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

    targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class.java)
        .map { target ->
            val mainCompilation = target.compilations.getByName("main")
            val executables =
                target.binaries.filterIsInstance<Executable>()

            Pair(mainCompilation, executables)
        }
        .forEach { pair ->
            if (!pair.second.isEmpty()) {

                pair.first.kotlinOptions.freeCompilerArgs += listOf(
                    "-linker-options", "-framework Sentry " +
                            "-F cli-build/native/Sentry.xcframework/macos-arm64_x86_64 -L/usr/lib/swift/ " +
                            "-L${xcodeInstallPath()}/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/macosx"
                )
            }
        }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.2")
                implementation("io.sentry:sentry-kotlin-multiplatform:0.7.1")
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

// Homebrew build
val brewbuild: String? by project

if (brewbuild == "true") {
    tasks.register<Exec>("mvDsymsArm") {
        commandLine("./mv-dsyms-arm.sh")
        group = "custom"
        description = "Uploads Sentry dSYMsn for arm/m1"
    }

    tasks.register<Exec>("mvDsymsIntel") {
        commandLine("./mv-dsyms-intel.sh")
        group = "custom"
        description = "Uploads Sentry dSYMsn for intel"
    }
    tasks.named("linkReleaseExecutableMacosArm64") { finalizedBy("mvDsymsArm") }
    tasks.named("linkReleaseExecutableMacosX64") { finalizedBy("mvDsymsIntel") }
}