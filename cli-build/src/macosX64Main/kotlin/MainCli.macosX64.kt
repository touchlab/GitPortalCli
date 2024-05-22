import co.touchlab.gitportal.Main
import kotlinx.cli.Subcommand

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MainCli actual constructor() {
    actual fun entry(args: Array<String>, vararg commands: (Any) -> Any) {
        Main().entry(args, *commands)
    }
}