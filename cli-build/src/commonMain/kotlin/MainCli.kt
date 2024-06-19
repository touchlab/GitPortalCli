import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.SentryLevel
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb

@Suppress("unused")
fun mainCli(args: Array<String>) {
    var eventCount = 0
    Sentry.init { options ->
        options.dsn = "https://6d5b8330a0eb011490ee99d821caa4d7@o326303.ingest.us.sentry.io/4507409102209024"
        options.debug = false
        options.release = gitPortalVersion
        options.beforeSend = { sentryEvent ->
            eventCount++
            sentryEvent
        }
        // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
        // We recommend adjusting this value in production.
        options.tracesSampleRate = 1.0
    }

    MainCli().entry(
        args = args,
        sentryEventCount = { eventCount },
        breadcrumbLogWriter = SentryLogWriter,
        { arg: Any -> DeployKeySetup(arg) }
    )
}

private object SentryLogWriter : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        Sentry.addBreadcrumb(
            Breadcrumb(
                level = when (severity) {
                    Severity.Verbose -> null
                    Severity.Debug -> SentryLevel.DEBUG
                    Severity.Info -> SentryLevel.INFO
                    Severity.Warn -> SentryLevel.WARNING
                    Severity.Error, Severity.Assert -> SentryLevel.FATAL
                },
                message = message,
                category = tag,
                type = if (throwable == null) {
                    "default"
                } else {
                    "error"
                },
                data = throwable?.let { ex ->
                    mutableMapOf(
                        Pair(
                            ex::class.simpleName ?: "(Unknown exception type)",
                            ex.stackTraceToString()
                        )
                    )
                }
            )
        )
    }
}

expect val gitPortalVersion: String

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class MainCli() {
    fun entry(
        args: Array<String>,
        sentryEventCount: () -> Int,
        breadcrumbLogWriter: LogWriter,
        vararg commands: (Any) -> Any
    )
}