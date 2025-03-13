package org.jetbrains.plugins.git.custompush.actions

import com.intellij.dvcs.ui.DvcsBundle
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.changes.actions.BaseCommitExecutorAction
import com.intellij.ui.components.JBOptionButton
import com.intellij.util.ui.JButtonAction
import kotlinx.coroutines.runBlocking
import org.jetbrains.plugins.git.custompush.executors.CustomCommitExecutor
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.JButton
import javax.swing.JComponent


class CustomGitButtonAction : AnAction(), CustomComponentAction {
    private val gitRepoAction: GitRepoAction = GitRepoAction()

    override fun actionPerformed(e: AnActionEvent) {
        val commitWorkflow = e.getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER)
        runBlocking {
            commitWorkflow?.execute(
                CustomCommitExecutor()
            )
        }
        gitRepoAction.perform(e.project ?: error("No project found"))
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        val mainAction = createMainButtonAction(presentation, place)
        val options = createOptionsArray()

        return JBOptionButton(mainAction, options).apply {
            text = presentation.text
            icon = presentation.icon
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    private fun createMainButtonAction(presentation: Presentation, place: String) = object : AbstractAction(presentation.text) {
        override fun actionPerformed(e: ActionEvent) {
            val dataContext = DataManager.getInstance().getDataContext(e.source as JComponent)
            val event = AnActionEvent.createFromAnAction(
                this@CustomGitButtonAction,
                null,
                place,
                dataContext
            )
            actionPerformed(event)
        }
    }

    private fun createOptionsArray(): Array<Action> = arrayOf(
        createOptionAction("Push with Options...") { e: ActionEvent ->
            val dataContext = DataManager.getInstance().getDataContext(e.source as JComponent)
            val project = CommonDataKeys.PROJECT.getData(dataContext)
            gitRepoAction.perform(project ?: error("No project found"))
        },
        createOptionAction("Commit and Push...") { e: ActionEvent ->
            val dataContext = DataManager.getInstance().getDataContext(e.source as JComponent)
            DefaultCommitAndPushAction().actionPerformed(AnActionEvent.createFromAnAction(
                this@CustomGitButtonAction,
                null,
                ActionPlaces.UNKNOWN,
                dataContext
            ))
        }
    )

    private fun createOptionAction(text: String, action: (e: ActionEvent) -> Unit) = object : AbstractAction(text) {
        override fun actionPerformed(e: ActionEvent) {
            action(e)
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.apply {
            text = "Commit && Custom Push..."
        }
    }
}

class PushWithOptionAction: JButtonAction("Push with Options...") {
    private val gitRepoAction: GitRepoAction = GitRepoAction()

    override fun createButton(): JButton = object : JButton() {}
    override fun actionPerformed(e: AnActionEvent) = gitRepoAction.perform(e.project ?: error("No project found"))

}

class DefaultCommitAndPushAction : BaseCommitExecutorAction() {
    init {
        templatePresentation.setText(DvcsBundle.messagePointer("action.commit.and.push.text"))
    }

    override val executorId: String = "Git.Commit.And.Push.Executor"
}
