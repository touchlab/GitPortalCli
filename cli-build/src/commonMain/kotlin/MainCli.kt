import io.sentry.kotlin.multiplatform.Sentry
import kotlin.random.Random

fun mainCli(args: Array<String>) {
    Sentry.init { options ->
        options.dsn = "https://6d5b8330a0eb011490ee99d821caa4d7@o326303.ingest.us.sentry.io/4507409102209024"
        options.debug = true // Enabling debug when first installing is always helpful
        options.release = gitPortalVersion

        // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
        // We recommend adjusting this value in production.
        options.tracesSampleRate = 1.0

    }

    println("isCrashedLastRun: ${Sentry.isCrashedLastRun()}")

    println("before")

    try {
        sleepMe(2)
        MainCli().entry(args, { arg: Any -> DeployKeySetup(arg) })
        sleepMe(2)
    } catch (e: Throwable) {
        sleepMe(2)
        Sentry.captureException(ShitheadException(e))
    }

    println("after")

    sleepMe(2000)

    forceWait()
}

expect fun sleepMe(ms:Long)

private fun forceWait() {
    println("Start forcing")
    sleepMe(5)
    println("End forcing")
    /*var forceWait = 0
    while (forceWait < 20_000_000) {
        val astring = "Hello $forceWait ${Random.nextLong()}"
        if (astring == "whatever") {
            throw NullPointerException("Nuts")
        }
        if (forceWait % 100_000 == 0) {
            println(astring)
        }
        forceWait++
    }*/
}

class ShitheadException(e:Throwable):Exception(cause = e)

expect val gitPortalVersion:String

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class MainCli() {
    fun entry(args: Array<String>, vararg commands: (Any) -> Any)
}