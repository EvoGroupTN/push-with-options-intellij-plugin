package org.jetbrains.plugins.git.custompush.ui

import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel


class AppSettingsComponent {
    val panel: JPanel
    private val isCommitAndPushButtonHidden = JBCheckBox("Hide \"Commit and Push...\" button in the commit tool window (needs restart)")

    init {
        panel = FormBuilder.createFormBuilder()
            .addComponent(isCommitAndPushButtonHidden, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    val preferredFocusedComponent: JComponent
        get() = isCommitAndPushButtonHidden

    var isCommitAndPushHidden: Boolean
        get() = isCommitAndPushButtonHidden.isSelected
        set(newStatus) {
            isCommitAndPushButtonHidden.isSelected = newStatus
        }
}