package org.jetbrains.plugins.git.custompush.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsNotifier
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitUtil
import git4idea.GitVcs
import git4idea.actions.GitRepositoryAction
import git4idea.commands.*
import git4idea.repo.GitBranchTrackInfo
import git4idea.repo.GitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.plugins.git.custompush.ui.GitPushDialog

class GitRepoAction : GitRepositoryAction() {
    private var trackInfo: GitBranchTrackInfo? = null
    private lateinit var repository: GitRepository

    override fun getActionName(): String {
        return "Push with options..."
    }

    override fun perform(project: Project,
                         virtualFiles: MutableList<VirtualFile>,
                         virtualFile: VirtualFile) {
        getRemoteBranchName(project, virtualFile)
        val dialog = GitPushDialog(project, trackInfo?.remoteBranch?.name, true)
        dialog.show()
        if (dialog.isOK()) {
            runPush(project, dialog.getPushOptions())
        }
    }

    override fun isEnabled(e: AnActionEvent?): Boolean {
        return true
    }

    private fun getRemoteBranchName(project: Project,
                                    virtualFile: VirtualFile) {
        runBlocking(Dispatchers.Default) {
            repository = project.let { repo ->
                GitUtil.getRepositoryForFile(repo, virtualFile)
            }
            trackInfo = GitUtil.getTrackInfoForCurrentBranch(repository)
        }
    }

    private fun runPush(project: Project,
                        pushOptions: List<String>) {
        val task: Task.Backgroundable =
            object : Task.Backgroundable(project, "push with options", false) {
                override fun run(indicator: ProgressIndicator) {
                    indicator.checkCanceled()
                    val result: GitCommandResult = push(repository, pushOptions)
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
        pushOptions: List<String>
    ): GitCommandResult {
        val gitRemote = trackInfo?.remote
        val url = gitRemote?.firstUrl ?: repository.remotes.firstOrNull()?.firstUrl
        val progressListener = GitStandardProgressAnalyzer.createListener(ZDummyProgressIndicator())
        val result = Git.getInstance().runCommand {
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
            } else {
                h.addParameters("origin")
                h.addParameters(repository.currentBranchName + ":" + repository.currentBranchName)
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