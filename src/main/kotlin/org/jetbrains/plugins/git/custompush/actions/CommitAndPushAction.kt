package org.jetbrains.plugins.git.custompush.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vcs.changes.actions.BaseCommitExecutorAction
import git4idea.actions.GitCommitAndPushExecutorAction
import org.jetbrains.plugins.git.custompush.settings.AppSettingsState

class CommitAndPushAction: BaseCommitExecutorAction() {

    private val commitAndPushAction = GitCommitAndPushExecutorAction()
    override val executorId: String
        get() = "Git.Commit.And.Push.Executor"

    override fun actionPerformed(e: AnActionEvent) {
        commitAndPushAction.actionPerformed(e)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = !AppSettingsState.instance.isCommitAndPushHidden
        if (AppSettingsState.instance.isCommitAndPushHidden) {
            return
        }
        commitAndPushAction.update(e)
    }
}