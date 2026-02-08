package org.jetbrains.plugins.git.custompush.actions

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.memberProperties

class GitRepoActionTest {

    private lateinit var action: GitRepoAction

    @Before
    fun setUp() {
        action = GitRepoAction()
    }

    @Test
    fun `test GitRepoAction has perform method`() {
        val performMethod = action::class.declaredMemberFunctions
            .find { it.name == "perform" }
        assertNotNull("GitRepoAction should have perform method", performMethod)
        
        // Verify the method signature
        val parameters = performMethod?.parameters
        assertNotNull("perform method should have parameters", parameters)
        assertEquals("perform method should have 2 parameters (this + Project)", 
            2, parameters?.size)
    }

    @Test
    fun `test GitRepoAction has required private methods`() {
        val methods = action::class.declaredMemberFunctions.map { it.name }
        
        assertTrue("GitRepoAction should have getRemoteBranchName method",
            methods.contains("getRemoteBranchName"))
        assertTrue("GitRepoAction should have runPush method",
            methods.contains("runPush"))
        assertTrue("GitRepoAction should have handleResult method",
            methods.contains("handleResult"))
        assertTrue("GitRepoAction should have push method",
            methods.contains("push"))
    }

    @Test
    fun `test GitRepoAction has trackInfo property`() {
        val properties = action::class.memberProperties.map { it.name }
        assertTrue("GitRepoAction should have trackInfo property",
            properties.contains("trackInfo"))
    }

    @Test
    fun `test GitRepoAction has repository property`() {
        val properties = action::class.memberProperties.map { it.name }
        assertTrue("GitRepoAction should have repository property",
            properties.contains("repository"))
    }

    @Test
    fun `test GitRepoAction is not abstract`() {
        // Verify it's a concrete class that can be instantiated
        assertFalse("GitRepoAction should not be abstract",
            action::class.isAbstract)
    }
}
