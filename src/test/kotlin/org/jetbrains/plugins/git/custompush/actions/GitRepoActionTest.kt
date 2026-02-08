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
    fun `test GitRepoAction can be instantiated`() {
        assertNotNull("GitRepoAction should be instantiable", action)
    }

    @Test
    fun `test GitRepoAction has perform method`() {
        val performMethod = action::class.declaredMemberFunctions
            .find { it.name == "perform" }
        assertNotNull("GitRepoAction should have perform method", performMethod)
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
    fun `test GitRepoAction class structure`() {
        // Verify the class has the expected structure for integration with IntelliJ Platform
        val className = action::class.simpleName
        assertEquals("Class name should be GitRepoAction", "GitRepoAction", className)
        
        // Verify it's not an abstract class or interface
        assertFalse("GitRepoAction should not be abstract",
            action::class.isAbstract)
    }

    @Test
    fun `test multiple GitRepoAction instances can be created`() {
        val action1 = GitRepoAction()
        val action2 = GitRepoAction()
        
        assertNotNull("First instance should not be null", action1)
        assertNotNull("Second instance should not be null", action2)
        assertNotSame("Different instances should not be the same object", action1, action2)
    }
}
