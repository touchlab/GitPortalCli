import co.touchlab.gitportal.Main
import co.touchlab.kermit.LogWriter
import io.sentry.kotlin.multiplatform.Sentry
import okio.Path.Companion.toPath

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MainCli actual constructor() {
    actual fun entry(
        args: Array<String>,
        sentryEventCount: () -> Int,
        breadcrumbLogWriter: LogWriter,
        vararg commands: (Any) -> Any
    ) {
        Main().entry(
            homePath = ".".toPath(),
            args = if (args.isEmpty()) {
                arrayOf("--help")
            } else {
                args
            },
            onAppError = { ex ->
                Sentry.captureException(ex)
            },
            sentryEventCount = sentryEventCount,
            breadcrumbLogWriter = breadcrumbLogWriter,
            commandFactories = commands
        )
    }
}

actual val gitPortalVersion: String
    get() = co.touchlab.gitportal.gitPortalVersion