package org.jetbrains.plugins.git.custompush.actions

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsNotifier
import git4idea.GitUtil
import git4idea.GitVcs
import git4idea.commands.*
import git4idea.repo.GitBranchTrackInfo
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.plugins.git.custompush.ui.GitPushDialog

class GitRepoAction {
    private var trackInfo: GitBranchTrackInfo? = null
    private lateinit var repository: GitRepository

    fun perform(project: Project) {
        getRemoteBranchName(project)
        val dialog = GitPushDialog(project, trackInfo?.remoteBranch?.name, true)
        dialog.show()
        if (dialog.isOK()) {
            try {
                runPush(project, dialog.getPushOptions(), dialog.getRemoteBranch())
            } catch (e: IllegalArgumentException) {
                VcsNotifier.getInstance(project).notifyError(
                    "git.custompush.error",
                    "Invalid branch name",
                    e.message ?: "Please enter a valid branch name"
                )
            }
        }
    }

    private fun getRemoteBranchName(project: Project) {
        runBlocking(Dispatchers.Default) {
            repository = GitRepositoryManager.getInstance(project).repositories.first() ?: error("No git repository found")
            trackInfo = GitUtil.getTrackInfoForCurrentBranch(repository)
        }
    }

    private fun runPush(project: Project,
                        pushOptions: List<String>,
                        remoteBranch: String) {
        val task: Task.Backgroundable =
            object : Task.Backgroundable(project, "push with options", false) {
                override fun run(indicator: ProgressIndicator) {
                    indicator.checkCanceled()
                    val result: GitCommandResult = push(repository, pushOptions, remoteBranch)
                    indicator.checkCanceled()
                    handleResult(project, result)
                }
            }
        GitVcs.runInBackground(task)
    }

    private fun handleResult(project: Project,
                             pushResult: GitCommandResult) {
        val vcsNotifier = VcsNotifier.getInstance(project)
        if (pushResult.success()) {
            vcsNotifier.notifySuccess(
                // extract strings to constants
                "git.custompush.success",
                "Push success",
                "Commits are pushed successfully"
            )
        } else {
            vcsNotifier.notifyError(
                "git.custompush.error",
                "Push error",
                "Error pushing commits.\n" + pushResult.errorOutputAsJoinedString
            )
        }
    }

    private fun push(
        repository: GitRepository,
        pushOptions: List<String>,
        remoteBranch: String
    ): GitCommandResult {
        val gitRemote = trackInfo?.remote
        val url = gitRemote?.firstUrl ?: repository.remotes.firstOrNull()?.firstUrl
        val progressListener = GitStandardProgressAnalyzer.createListener(ZDummyProgressIndicator())
        val result = Git.getInstance()
            .runCommand {
            val h = GitLineHandler(
                repository.project, repository.root,
                GitCommand.PUSH
            )
            if (url != null) h.setUrl(url)
            h.setSilent(false)
            h.setStdoutSuppressed(false)
            h.addLineListener(progressListener)
            if (gitRemote?.name != null) {
                h.addParameters(gitRemote.name)
                h.addParameters("HEAD:$remoteBranch")
            } else {
                h.addParameters("--set-upstream")
                h.addParameters("origin")
                h.addParameters("HEAD:$remoteBranch")
            }
            h.addParameters("--progress")
            pushOptions.forEach { option ->
                h.addParameters(option)
            }
            h
        }
        return result
    }
}