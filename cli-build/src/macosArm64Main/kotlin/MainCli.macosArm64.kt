import co.touchlab.gitportal.Main

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MainCli actual constructor() {
    actual fun entry(args: Array<String>, vararg commands: (Any) -> Any) {
        Main().entry(
            if (args.isEmpty()) {
                arrayOf("--help")
            } else {
                args
            }, *commands
        )
    }
}