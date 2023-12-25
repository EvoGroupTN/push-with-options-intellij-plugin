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
import git4idea.repo.GitRemote
import git4idea.repo.GitRepository
import org.jetbrains.plugins.git.custompush.ui.GitPushDialog

class GitRepoAction: GitRepositoryAction() {

    override fun getActionName(): String {
        return "Push with options..."
    }

    override fun perform(project: Project, virtualFiles: MutableList<VirtualFile>, virtualFile: VirtualFile) {
        val dialog = GitPushDialog(true)
        dialog.show()
        if (dialog.isOK()) {
            runPush(project, virtualFile, dialog.getPushOptions())
        }
    }

    override fun isEnabled(e: AnActionEvent?): Boolean {
        return true
    }

    private fun runPush(project: Project, virtualFile: VirtualFile, pushOptions: List<String>) {
        val task: Task.Backgroundable =
            object : Task.Backgroundable(project, "push with options", false) {
                override fun run(indicator: ProgressIndicator) {
                    indicator.checkCanceled()
                    val repository = project.let { repo ->
                        GitUtil.getRepositoryForFile(repo, virtualFile)
                    }
                    val trackInfo = GitUtil.getTrackInfoForCurrentBranch(repository)
                    val gitRemote = trackInfo?.remote
                    val url = gitRemote?.firstUrl;
                    if (trackInfo == null || gitRemote == null || url == null) {
                        throw Exception("Repository Exception")
                    }
                    val result: GitCommandResult = push(repository, gitRemote, url, pushOptions)
                    indicator.checkCanceled()
                    handleResult(project, result)
                }
            }
        GitVcs.runInBackground(task)
    }

    private fun handleResult(project: Project, pushResult: GitCommandResult) {
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
        remote: GitRemote,
        url: String,
        pushOptions: List<String>
    ): GitCommandResult {
        val progressListener = GitStandardProgressAnalyzer.createListener(ZDummyProgressIndicator())
        val result = Git.getInstance().runCommand {
            val h = GitLineHandler(
                repository.project, repository.root,
                GitCommand.PUSH
            )
            h.setUrl(url)
            h.setSilent(false)
            h.setStdoutSuppressed(false)
            h.addLineListener(progressListener)
            h.addParameters("--progress")
            h.addParameters(remote.name)
            pushOptions.forEach {
                option -> h.addParameters(option)
            }
            h
        }
        return result
    }
}