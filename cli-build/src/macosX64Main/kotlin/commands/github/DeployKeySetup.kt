
import co.touchlab.gitportal.GitPortal
import co.touchlab.gitportal.commands.CommandError
import co.touchlab.gitportal.commands.commandError
import co.touchlab.gitportal.process.procException
import co.touchlab.gitportal.process.readText
import co.touchlab.gitportal.utils.consoleOut
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.multiple
import kotlinx.cli.required
import okio.FileSystem
import kotlin.random.Random

actual class DeployKeySetup actual constructor(gitPortalArg: Any) : co.touchlab.gitportal.commands.BaseLoggingCommand("deploykey", "Create Deploy Key and secrets") {
    private val gitPortal: GitPortal = gitPortalArg as GitPortal
    val kmpRepo by option(ArgType.String, shortName = "k", description = "GitHub KMP Repo [org/repo]").required()
    val appRepos by option(ArgType.String, shortName = "a", description = "GitHub App Repos [org/repo]").multiple()
    val keyname by option(
        ArgType.String,
        shortName = "kn",
        description = "Deploy Key Label. Default 'GitPortal Key'"
    ).default("GitPortal Key")

    override fun executeWithErrorLogging() {
        checkRepoAccess()

        val deployKeyName = "dp-${Random.nextLong()}" // It would be *possible* to get the same random val, but unlikely
        val deployKeyPrivateFilePath = gitPortal.localConfigManager.localTempFile(deployKeyName)
        val deployKeyPublicFilePath =gitPortal.localConfigManager.localTempFile("${deployKeyName}.pub")

        gitPortal.procRunOut(
            "ssh-keygen",
            "-t",
            "ed25519",
            "-f",
            deployKeyPrivateFilePath.toString(),
            "-C",
            "git@github.com:$kmpRepo",
            "-P",
            ""
        )

        try {
            gitPortal.logger.i("Adding Deploy Key to $kmpRepo")
            ghCall(
                "gh",
                "repo",
                "deploy-key",
                "add",
                deployKeyPublicFilePath.toString(),
                "-w",
                "-R",
                kmpRepo,
                "-t",
                keyname
            )
            val secretVal = gitPortal.makeFile(deployKeyPrivateFilePath).readText()
            appRepos.forEach { repo ->
                ghCall("gh", "secret", "set", "GITPORTAL_SSH_KEY", "--body", secretVal, "-R", repo)
            }
        } finally {
            FileSystem.SYSTEM.delete(
                path = deployKeyPrivateFilePath,
                mustExist = false
            )
            FileSystem.SYSTEM.delete(
                path = deployKeyPublicFilePath,
                mustExist = false
            )
        }

        consoleOut("Deploy keys and secrets created successfully")
    }

    private fun checkRepoAccess() {
        val argList = listOf("gh", "repo", "deploy-key", "list", "-R")
        ghCall(*(argList + kmpRepo).toTypedArray())

        appRepos.forEach { repo ->
            ghCall(*(argList + repo).toTypedArray())
        }
    }

    private fun ghCall(vararg args: String): Result<String> {
        val result = gitPortal.runNoFail {
            gitPortal.procRunOut(
                *args
            )
        }

        if (result.isFailure) {
            if (result.procException?.combinedOutput?.contains("gh auth login") == true) {
                commandError(CommandError.GitHubNeedsAuth, "GitHub Cli is not authorized. Run 'gh auth login' and follow the instructions.")
            }
            if (result.procException?.combinedOutput?.contains("Could not resolve to a Repository with the name") == true) {
                commandError(CommandError.GitHubNeedsAuth, "GitHub repo not found.")
            }
            if (result.procException?.combinedOutput?.contains("HTTP 404: Not Found") == true) {
                commandError(CommandError.GitHubRepoInsufficientAccess, "GitHub Cli reported 'Not Found'. You may not have sufficient access to this repo.")
            }
        }

        return result
    }
}