package org.jetbrains.plugins.git.custompush.executors

import com.intellij.dvcs.commit.getCommitAndPushActionName
import com.intellij.openapi.util.Key
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.changes.CommitExecutorWithRichDescription
import com.intellij.openapi.vcs.changes.CommitSession
import com.intellij.vcs.commit.CommitWorkflowHandlerState
import com.intellij.vcs.commit.commitExecutorProperty
import org.jetbrains.annotations.Nls


private val IS_PUSH_AFTER_COMMIT_KEY = Key.create<Boolean>("Git.Commit.IsPushAfterCommit")
internal var CommitContext.isPushAfterCommit: Boolean by commitExecutorProperty(IS_PUSH_AFTER_COMMIT_KEY)

class CustomCommitExecutor : CommitExecutorWithRichDescription {
    @Nls
    override fun getActionText(): String = "Commit and Push with Options..."

    override fun getText(state: CommitWorkflowHandlerState): String {
        return getCommitAndPushActionName(state)
    }

    override fun useDefaultAction(): Boolean = false

    override fun requiresSyncCommitChecks(): Boolean = true

    override fun getId(): String = ID

    override fun supportsPartialCommit(): Boolean = true

    override fun createCommitSession(commitContext: CommitContext): CommitSession {
        commitContext.isPushAfterCommit = false
        return CommitSession.VCS_COMMIT
    }

    companion object {
        internal const val ID = "Git.Commit.And.Push.With.Options.Executor"
    }
}