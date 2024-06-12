import io.sentry.kotlin.multiplatform.Sentry

fun mainCli(args: Array<String>) {
    Sentry.init { options ->
        options.dsn = "https://6d5b8330a0eb011490ee99d821caa4d7@o326303.ingest.us.sentry.io/4507409102209024"
        options.debug = false // Enabling debug when first installing is always helpful

        // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
        // We recommend adjusting this value in production.
        options.tracesSampleRate = 1.0
    }

    MainCli().entry(args, { arg: Any -> DeployKeySetup(arg) })
}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class MainCli() {
    fun entry(args: Array<String>, vararg commands: (Any) -> Any)
}