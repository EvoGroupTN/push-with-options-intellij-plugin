package org.jetbrains.plugins.git.custompush.settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import org.jetbrains.plugins.git.custompush.ui.AppSettingsComponent
import javax.swing.JComponent


/**
 * Provides controller functionality for application settings.
 */
internal class AppSettingsConfigurable : Configurable {
    private var mySettingsComponent: AppSettingsComponent? = null

    // A default constructor with no arguments is required because this implementation
    // is registered in an applicationConfigurable EP
    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "Git Custom Push"
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return mySettingsComponent!!.preferredFocusedComponent
    }

    override fun createComponent(): JComponent? {
        mySettingsComponent = AppSettingsComponent()
        return mySettingsComponent!!.panel
    }

    override fun isModified(): Boolean {
        val settings: AppSettingsState = AppSettingsState.instance
        var modified: Boolean = mySettingsComponent?.isCommitAndPushHidden != settings.isCommitAndPushHidden
        return modified
    }

    override fun apply() {
        val settings: AppSettingsState = AppSettingsState.instance
        settings.isCommitAndPushHidden = mySettingsComponent?.isCommitAndPushHidden == true
    }

    override fun reset() {
        val settings: AppSettingsState = AppSettingsState.instance
        mySettingsComponent?.isCommitAndPushHidden = settings.isCommitAndPushHidden
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}