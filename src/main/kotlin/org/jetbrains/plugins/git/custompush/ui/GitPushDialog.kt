package org.jetbrains.plugins.git.custompush.ui

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.io.File
import javax.swing.Action
import javax.swing.JComponent

class GitPushDialog(project: Project, private var remoteBranch: String?, canBeParent: Boolean) : DialogWrapper(canBeParent) {
    private val OTHER_OPTIONS_KEY = "other_options"
    private val OTHER_OPTIONS_TEXT_KEY = "other_options_text"
    private val PUSH_OPTIONS_FILE = ".push-options"

    private var panel: JBPanel<JBPanel<*>>? = null
    private val remoteBranchTextField: JBTextField
    private val otherOptionsCheckBox: JBCheckBox
    private val otherOptionsTextField: JBTextField
    private val checkboxes = ArrayList<JBCheckBox>()

    init {
        remoteBranch = remoteBranch?:"new branch"
        title = "Push Options"
        
        // Initialize remote branch text field
        remoteBranchTextField = JBTextField()
        remoteBranchTextField.text = remoteBranch
        
        var pushOptionsFile = File(project.basePath + "/" + PUSH_OPTIONS_FILE)
        if (!(pushOptionsFile.exists() && pushOptionsFile.isFile()))
            pushOptionsFile = File(System.getProperty("user.home") + "/" + PUSH_OPTIONS_FILE)
        if (pushOptionsFile.exists() && pushOptionsFile.isFile()) {
            pushOptionsFile.useLines {
                lines -> lines.filter { !it.startsWith("#") }.forEach {
                    checkboxes.add(JBCheckBox(it))
                }
            }
        }
        panel = JBPanel<JBPanel<*>>(GridLayout(checkboxes.size + 4, 1))
        
        // Add remote branch field with label
        val remoteBranchLabel = com.intellij.ui.components.JBLabel("Remote branch:")
        panel!!.add(remoteBranchLabel)
        panel!!.add(remoteBranchTextField)
        
        checkboxes.forEach {
            panel!!.add(it)
        }
        otherOptionsCheckBox = JBCheckBox("Other options")
        otherOptionsTextField = JBTextField()
        otherOptionsTextField.isEnabled = false
        otherOptionsCheckBox.addItemListener { _ ->
            otherOptionsTextField.isEnabled = otherOptionsCheckBox.isSelected
            otherOptionsTextField.isEditable = otherOptionsCheckBox.isSelected
        }

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
        checkboxes.forEach {
            if (it.isSelected) pushOptions.add(it.text)
        }
        if (otherOptionsCheckBox.isSelected) pushOptions.add(otherOptionsTextField.text)
        return pushOptions
    }
    
    fun getRemoteBranch(): String {
        val branchName = remoteBranchTextField.text.trim()
        // Validate branch name to prevent command injection and ensure valid Git branch names
        // Git branch names rules:
        // - Cannot be empty
        // - Can contain alphanumeric, /, -, _, . characters
        // - Cannot contain consecutive dots (..)
        // - Cannot start or end with ., /, or -
        if (branchName.isEmpty()) {
            throw IllegalArgumentException("Branch name cannot be empty")
        }
        if (!branchName.matches(Regex("^[a-zA-Z0-9/_.-]+$"))) {
            throw IllegalArgumentException("Branch name contains invalid characters")
        }
        if (branchName.contains("..")) {
            throw IllegalArgumentException("Branch name cannot contain consecutive dots (..)")
        }
        if (branchName.startsWith(".") || branchName.endsWith(".") || 
            branchName.startsWith("/") || branchName.endsWith("/") ||
            branchName.startsWith("-") || branchName.endsWith("-")) {
            throw IllegalArgumentException("Branch name cannot start or end with '.', '/', or '-'")
        }
        return branchName
    }

    private fun loadSavedSettings() {
        val propertiesComponent = PropertiesComponent.getInstance()
        checkboxes.forEach {
            it.isSelected = propertiesComponent.getBoolean(it.text.filter { !it.isWhitespace() })
        }
        otherOptionsCheckBox.isSelected = propertiesComponent.getBoolean(OTHER_OPTIONS_KEY, false)
        otherOptionsTextField.text = propertiesComponent.getValue(OTHER_OPTIONS_TEXT_KEY, "")
        otherOptionsTextField.isEnabled = otherOptionsCheckBox.isSelected
        otherOptionsTextField.isEditable = otherOptionsCheckBox.isSelected
        
        // Note: We don't load saved remote branch to avoid confusion
        // The dialog always shows the current tracked branch or "new branch" as default
    }

    private fun saveSettings() {
        val propertiesComponent = PropertiesComponent.getInstance()
        checkboxes.forEach {
            propertiesComponent.setValue(it.text.filter { !it.isWhitespace() }, it.isSelected)
        }
        propertiesComponent.setValue(OTHER_OPTIONS_KEY, otherOptionsCheckBox.isSelected.toString())
        propertiesComponent.setValue(OTHER_OPTIONS_TEXT_KEY, otherOptionsTextField.text)
        // Note: We don't save remote branch as it should be determined per push operation
    }
}