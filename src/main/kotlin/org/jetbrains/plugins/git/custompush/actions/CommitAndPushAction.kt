package org.jetbrains.plugins.git.custompush.actions

import com.intellij.openapi.vcs.changes.actions.BaseCommitExecutorAction
import org.jetbrains.plugins.git.custompush.settings.AppSettingsState

class CommitAndPushAction: BaseCommitExecutorAction() {
    override val executorId: String =
        if (AppSettingsState.instance.isCommitAndPushHidden) {
            "git.custompush.toolwindow.button"
        } else {
            "Git.Commit.And.Push.Executor"
        }
}