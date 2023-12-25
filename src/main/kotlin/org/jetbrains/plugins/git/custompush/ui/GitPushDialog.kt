package org.jetbrains.plugins.git.custompush.ui;

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import java.awt.GridLayout
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JComponent

class GitPushDialog(canBeParent: Boolean) : DialogWrapper(canBeParent) {
    private val FORCE_PUSH_KEY = "force_push"
    private val SKIP_CI_KEY = "skip_ci"
    private val FULL_PIPELINE_KEY = "full_pipeline"
    private val OTHER_OPTIONS_KEY = "other_options"
    private val OTHER_OPTIONS_TEXT_KEY = "other_options_text"

    private var panel: JBPanel<JBPanel<*>>? = null
    private val forceCheckbox: JBCheckBox
    private val skipCiCheckbox: JBCheckBox
    private val fullPipelineCheckbox: JBCheckBox
    private val otherOptionsCheckBox: JBCheckBox
    private val otherOptionsTextField: JBTextField

    init {
        title = "Push..."
        panel = JBPanel<JBPanel<*>>(GridLayout(5, 1))
        forceCheckbox = JBCheckBox("--force-with-lease")
        skipCiCheckbox = JBCheckBox("-o ci.skip")
        fullPipelineCheckbox = JBCheckBox("-o ci.variable=\"FULL_PIPELINE=true\"")
        otherOptionsCheckBox = JBCheckBox("Other options")
        otherOptionsTextField = JBTextField()
        otherOptionsTextField.setEnabled(false)
        otherOptionsCheckBox.addItemListener { e ->
            otherOptionsTextField.setEnabled(otherOptionsCheckBox.isSelected())
            otherOptionsTextField.setEditable(otherOptionsCheckBox.isSelected())
        }

        panel!!.add(forceCheckbox)
        panel!!.add(skipCiCheckbox)
        panel!!.add(fullPipelineCheckbox)
        panel!!.add(otherOptionsCheckBox)
        panel!!.add(otherOptionsTextField)
        init()
        loadSavedSettings()
    }

    override fun createCenterPanel(): JComponent? {
        return panel
    }

    override fun getOKAction(): Action {
        return object: OkAction() {
            init {
                putValue("Name", "Push")
            }
            override fun actionPerformed(e: ActionEvent?) {
                saveSettings()
                super.actionPerformed(e)
            }

        }
    }

    fun getPushOptions(): List<String> {
        val pushOptions = ArrayList<String>()
        if (forceCheckbox.isSelected()) pushOptions.add(forceCheckbox.text)
        if (skipCiCheckbox.isSelected()) pushOptions.add(skipCiCheckbox.text)
        if (fullPipelineCheckbox.isSelected()) pushOptions.add(fullPipelineCheckbox.text)
        if (otherOptionsCheckBox.isSelected()) pushOptions.add(otherOptionsTextField.text)
        return pushOptions
    }

    private fun loadSavedSettings() {
        val propertiesComponent = PropertiesComponent.getInstance()
        forceCheckbox.isSelected = propertiesComponent.getBoolean(FORCE_PUSH_KEY, false)
        skipCiCheckbox.isSelected = propertiesComponent.getBoolean(SKIP_CI_KEY, false)
        fullPipelineCheckbox.isSelected = propertiesComponent.getBoolean(FULL_PIPELINE_KEY, false)
        otherOptionsCheckBox.isSelected = propertiesComponent.getBoolean(OTHER_OPTIONS_KEY, false)
        otherOptionsTextField.text = propertiesComponent.getValue(OTHER_OPTIONS_TEXT_KEY, "")
        otherOptionsTextField.isEnabled = otherOptionsCheckBox.isSelected
        otherOptionsTextField.isEditable = otherOptionsCheckBox.isSelected
    }

    private fun saveSettings() {
        val propertiesComponent = PropertiesComponent.getInstance()
        propertiesComponent.setValue(FORCE_PUSH_KEY, forceCheckbox.isSelected.toString())
        propertiesComponent.setValue(SKIP_CI_KEY, skipCiCheckbox.isSelected.toString())
        propertiesComponent.setValue(FULL_PIPELINE_KEY, fullPipelineCheckbox.isSelected.toString())
        propertiesComponent.setValue(OTHER_OPTIONS_KEY, otherOptionsCheckBox.isSelected.toString())
        propertiesComponent.setValue(OTHER_OPTIONS_TEXT_KEY, otherOptionsTextField.text)
    }
}