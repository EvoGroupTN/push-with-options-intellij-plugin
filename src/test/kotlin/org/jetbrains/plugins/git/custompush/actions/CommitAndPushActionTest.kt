package org.jetbrains.plugins.git.custompush.actions

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class CommitAndPushActionTest {

    private lateinit var action: CommitAndPushAction

    @Before
    fun setUp() {
        action = CommitAndPushAction()
    }

    @Test
    fun `test executorId property exists`() {
        val property = action::class.memberProperties.find { it.name == "executorId" }
        assertNotNull("executorId property should exist", property)
    }

    @Test
    fun `test executorId is not empty`() {
        val property = action::class.memberProperties.find { it.name == "executorId" }
        assertNotNull("executorId property should exist", property)
        
        // Make property accessible to read protected field
        property!!.isAccessible = true
        val executorId = property.call(action) as String
        assertTrue("executorId should not be empty", executorId.isNotEmpty())
    }

    @Test
    fun `test executorId is valid identifier format`() {
        val property = action::class.memberProperties.find { it.name == "executorId" }
        assertNotNull("executorId property should exist", property)
        
        // Make property accessible to read protected field
        property!!.isAccessible = true
        val executorId = property.call(action) as String
        
        // executorId should be a valid identifier string (contains letters, digits, and dots)
        assertTrue("executorId should be a valid identifier string", 
            executorId.matches(Regex("^[a-zA-Z0-9.]+$")))
    }
}
