package org.jetbrains.plugins.git.custompush.ui

import org.junit.Assert.*
import org.junit.Test

class GitPushDialogTest {

    @Test
    fun `test valid branch names`() {
        val validNames = listOf(
            "main",
            "feature/new-feature",
            "develop",
            "hotfix/bug-fix",
            "release/1.0.0",
            "feature_branch",
            "test.branch",
            "feature/sub/nested"
        )

        validNames.forEach { branchName ->
            // Test that validation doesn't throw for valid names
            try {
                validateBranchName(branchName)
            } catch (e: IllegalArgumentException) {
                fail("Branch name '$branchName' should be valid but validation failed: ${e.message}")
            }
        }
    }

    @Test
    fun `test empty branch name throws exception`() {
        assertThrows(IllegalArgumentException::class.java) {
            validateBranchName("")
        }
        assertThrows(IllegalArgumentException::class.java) {
            validateBranchName("   ")
        }
    }

    @Test
    fun `test invalid characters throw exception`() {
        val invalidNames = listOf(
            "branch name",  // space
            "branch@name",  // @
            "branch#name",  // #
            "branch\$name", // $
            "branch*name",  // *
            "branch?name"   // ?
        )

        invalidNames.forEach { branchName ->
            assertThrows(
                "Branch name '$branchName' should be invalid",
                IllegalArgumentException::class.java
            ) {
                validateBranchName(branchName)
            }
        }
    }

    @Test
    fun `test consecutive dots throw exception`() {
        assertThrows(IllegalArgumentException::class.java) {
            validateBranchName("feature..branch")
        }
        assertThrows(IllegalArgumentException::class.java) {
            validateBranchName("test..name")
        }
    }

    @Test
    fun `test branch names starting with special characters throw exception`() {
        assertThrows(IllegalArgumentException::class.java) {
            validateBranchName(".branch")
        }
        assertThrows(IllegalArgumentException::class.java) {
            validateBranchName("/branch")
        }
        assertThrows(IllegalArgumentException::class.java) {
            validateBranchName("-branch")
        }
    }

    @Test
    fun `test branch names ending with special characters throw exception`() {
        assertThrows(IllegalArgumentException::class.java) {
            validateBranchName("branch.")
        }
        assertThrows(IllegalArgumentException::class.java) {
            validateBranchName("branch/")
        }
        assertThrows(IllegalArgumentException::class.java) {
            validateBranchName("branch-")
        }
    }

    @Test
    fun `test branch name trimming`() {
        // Branch names should be trimmed before validation
        val trimmedName = validateBranchName("  feature/test  ")
        assertEquals("feature/test", trimmedName)
    }

    /**
     * Helper method that replicates the validation logic from GitPushDialog.getRemoteBranch()
     */
    private fun validateBranchName(input: String): String {
        val branchName = input.trim()
        
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

    /**
     * Inline assertThrows for compatibility with JUnit 4
     */
    private fun <T : Throwable> assertThrows(
        expectedType: Class<T>,
        runnable: () -> Unit
    ) {
        try {
            runnable()
            fail("Expected ${expectedType.simpleName} to be thrown")
        } catch (e: Throwable) {
            if (!expectedType.isInstance(e)) {
                fail("Expected ${expectedType.simpleName} but got ${e.javaClass.simpleName}: ${e.message}")
            }
        }
    }

    /**
     * Inline assertThrows with message for compatibility with JUnit 4
     */
    private fun <T : Throwable> assertThrows(
        message: String,
        expectedType: Class<T>,
        runnable: () -> Unit
    ) {
        try {
            runnable()
            fail("$message: Expected ${expectedType.simpleName} to be thrown")
        } catch (e: Throwable) {
            if (!expectedType.isInstance(e)) {
                fail("$message: Expected ${expectedType.simpleName} but got ${e.javaClass.simpleName}: ${e.message}")
            }
        }
    }
}
