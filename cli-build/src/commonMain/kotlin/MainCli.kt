import kotlinx.cli.Subcommand

fun mainCli(args: Array<String>) {
    MainCli().entry(args, { arg: Any -> DeployKeySetup(arg) })
}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class MainCli() {
    fun entry(args: Array<String>, vararg commands: (Any) -> Any)
}