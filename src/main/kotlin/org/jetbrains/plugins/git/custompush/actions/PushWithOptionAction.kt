package org.jetbrains.plugins.git.custompush.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.util.ui.JButtonAction
import javax.swing.JButton

class PushWithOptionAction: JButtonAction("Push with Options...") {
    private val gitRepoAction: GitRepoAction = GitRepoAction()

    override fun createButton(): JButton = object : JButton() {}

    override fun actionPerformed(e: AnActionEvent) = gitRepoAction.actionPerformed(e)

}