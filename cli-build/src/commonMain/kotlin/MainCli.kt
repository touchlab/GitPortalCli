import io.sentry.kotlin.multiplatform.Sentry

fun mainCli(args: Array<String>) {
    var eventCount = 0
    Sentry.init { options ->
        options.dsn = "https://6d5b8330a0eb011490ee99d821caa4d7@o326303.ingest.us.sentry.io/4507409102209024"
        options.debug = true // Enabling debug when first installing is always helpful
        options.release = gitPortalVersion

        options.beforeSend = { sentryEvent ->
            eventCount++
            println("Sentry event. Count: $eventCount")
            sentryEvent
        }
        // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
        // We recommend adjusting this value in production.
        options.tracesSampleRate = 1.0
    }

    try {
        MainCli().entry(args, { eventCount }, { arg: Any -> DeployKeySetup(arg) })
    } catch (e: Throwable) {
        Sentry.captureException(e)
    }
}

expect val gitPortalVersion: String

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class MainCli() {
    fun entry(args: Array<String>, sentryEventCount: () -> Int, vararg commands: (Any) -> Any)
}