import co.touchlab.gitportal.Main
import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.SentryLevel
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb
import okio.Path.Companion.toPath

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MainCli actual constructor() {
    actual fun entry(args: Array<String>, vararg commands: (Any) -> Any) {
        Main().entry(homePath = ".".toPath(),
            args = if (args.isEmpty()) {
                arrayOf("--help")
            } else {
                args
            },
            onAppError = {ex ->
                Sentry.addBreadcrumb(Breadcrumb(level = SentryLevel.ERROR, message = "Console error out. Type: ${ex.error}"))
                Sentry.captureException(ex)
            },
            commandFactories = commands
        )
    }
}