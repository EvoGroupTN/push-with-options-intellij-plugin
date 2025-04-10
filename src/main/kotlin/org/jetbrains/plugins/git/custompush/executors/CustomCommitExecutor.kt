package org.jetbrains.plugins.git.custompush.executors

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.changes.CommitExecutor
import com.intellij.openapi.vcs.changes.CommitSession
import com.intellij.openapi.vcs.checkin.*
import com.intellij.vcs.commit.commitExecutorProperty
import org.jetbrains.annotations.Nls
import org.jetbrains.plugins.git.custompush.actions.GitRepoAction


private val IS_PUSH_AFTER_COMMIT_KEY = Key.create<Boolean>("Git.Commit.IsPushAfterCommit")
internal var CommitContext.isPushAfterCommit: Boolean by commitExecutorProperty(IS_PUSH_AFTER_COMMIT_KEY)

@Service(
    Service.Level.PROJECT,
)
class CustomCommitExecutor : CommitExecutor {
    var isCustomPushAfterCommit: Boolean = false
    @Nls
    override fun getActionText(): String = "Commit and Push with Options..."

    override fun useDefaultAction(): Boolean = false

    override fun requiresSyncCommitChecks(): Boolean = true

    override fun getId(): String = ID

    override fun supportsPartialCommit(): Boolean = true

    override fun createCommitSession(commitContext: CommitContext): CommitSession {
        commitContext.isPushAfterCommit = false
        isCustomPushAfterCommit = true
        return CommitSession.VCS_COMMIT
    }

    companion object {
        internal const val ID = "Git.Commit.And.Push.With.Options.Executor"
    }
}

class CustomCheckHandler(private val project: Project): CheckinHandler() {

    override fun checkinSuccessful() {
        if (project.getService(CustomCommitExecutor::class.java)?.isCustomPushAfterCommit == true) {
            GitRepoAction().perform(project)
            project.getService(CustomCommitExecutor::class.java)?.isCustomPushAfterCommit = false
        }
    }

}

class CustomCheckinHandlerFactory : CheckinHandlerFactory() {
    override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
        return CustomCheckHandler(panel.project)
    }
}