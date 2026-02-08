package org.jetbrains.plugins.git.custompush.settings

import org.junit.Assert.*
import org.junit.Test

class AppSettingsStateTest {

    @Test
    fun `test default state is false`() {
        val state = AppSettingsState()
        assertFalse("Default isCommitAndPushHidden should be false", state.isCommitAndPushHidden)
    }

    @Test
    fun `test state can be set to true`() {
        val state = AppSettingsState()
        state.isCommitAndPushHidden = true
        assertTrue("isCommitAndPushHidden should be true", state.isCommitAndPushHidden)
    }

    @Test
    fun `test state can be set to false`() {
        val state = AppSettingsState()
        state.isCommitAndPushHidden = true
        state.isCommitAndPushHidden = false
        assertFalse("isCommitAndPushHidden should be false", state.isCommitAndPushHidden)
    }

    @Test
    fun `test getState returns self`() {
        val state = AppSettingsState()
        val returnedState = state.getState()
        assertNotNull("getState should not return null", returnedState)
        assertSame("getState should return the same instance", state, returnedState)
    }

    @Test
    fun `test loadState copies properties`() {
        val originalState = AppSettingsState()
        originalState.isCommitAndPushHidden = true

        val newState = AppSettingsState()
        newState.loadState(originalState)

        assertTrue("loadState should copy isCommitAndPushHidden property", newState.isCommitAndPushHidden)
    }

    @Test
    fun `test loadState with false value`() {
        val originalState = AppSettingsState()
        originalState.isCommitAndPushHidden = false

        val newState = AppSettingsState()
        newState.isCommitAndPushHidden = true
        newState.loadState(originalState)

        assertFalse("loadState should copy false value correctly", newState.isCommitAndPushHidden)
    }

    @Test
    fun `test multiple state changes`() {
        val state = AppSettingsState()
        
        assertFalse("Initial state should be false", state.isCommitAndPushHidden)
        
        state.isCommitAndPushHidden = true
        assertTrue("State should be true after setting to true", state.isCommitAndPushHidden)
        
        state.isCommitAndPushHidden = false
        assertFalse("State should be false after setting to false", state.isCommitAndPushHidden)
        
        state.isCommitAndPushHidden = true
        assertTrue("State should be true after setting to true again", state.isCommitAndPushHidden)
    }
}
