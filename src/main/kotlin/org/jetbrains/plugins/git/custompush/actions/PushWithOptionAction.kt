package org.jetbrains.plugins.git.custompush.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.util.ui.JButtonAction

class PushWithOptionAction: JButtonAction("Push with Options...") {
    private val gitRepoAction: GitRepoAction = GitRepoAction()
    override fun actionPerformed(p0: AnActionEvent) {
        return gitRepoAction.actionPerformed(p0)
    }

}