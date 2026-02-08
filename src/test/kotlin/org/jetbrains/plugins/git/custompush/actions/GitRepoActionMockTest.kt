package org.jetbrains.plugins.git.custompush.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRemote
import git4idea.repo.GitRepository
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GitRepoActionMockTest {

    private lateinit var action: GitRepoAction
    private lateinit var mockGit: Git
    private lateinit var mockRepository: GitRepository
    private lateinit var mockProject: Project
    private lateinit var mockRoot: VirtualFile
    private lateinit var mockRemote: GitRemote
    private lateinit var mockResult: GitCommandResult

    @Before
    fun setUp() {
        // Create the action instance
        action = GitRepoAction()
        
        // Mock the Git singleton
        mockGit = mockk()
        mockkStatic(Git::class)
        every { Git.getInstance() } returns mockGit
        
        // Mock repository and related objects
        mockRepository = mockk()
        mockProject = mockk()
        mockRoot = mockk()
        mockRemote = mockk()
        mockResult = mockk()
        
        // Setup common mock behaviors
        every { mockRepository.project } returns mockProject
        every { mockRepository.root } returns mockRoot
        every { mockResult.success() } returns true
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `test push method calls Git runCommand with correct parameters for remote branch`() {
        // Given
        val remoteBranch = "feature/test-branch"
        val pushOptions = listOf("--force-with-lease", "-o ci.skip")
        val remoteUrl = "git@github.com:test/repo.git"
        val remoteName = "origin"
        
        // Setup mocks
        every { mockRemote.name } returns remoteName
        every { mockRemote.firstUrl } returns remoteUrl
        every { mockRepository.remotes } returns listOf(mockRemote)
        
        // Capture the handler passed to runCommand
        val handlerSlot = slot<() -> GitLineHandler>()
        every { mockGit.runCommand(capture(handlerSlot)) } answers {
            val handler = handlerSlot.captured.invoke()
            
            // Verify handler setup
            assertEquals(GitCommand.PUSH, handler.command())
            
            // Verify parameters contain remote name and branch
            val params = handler.parameters()
            assertTrue(params.contains(remoteName), "Parameters should contain remote name '$remoteName'")
            assertTrue(params.contains("HEAD:$remoteBranch"), "Parameters should contain 'HEAD:$remoteBranch'")
            assertTrue(params.contains("--progress"), "Parameters should contain '--progress'")
            assertTrue(params.contains("--force-with-lease"), "Parameters should contain '--force-with-lease'")
            assertTrue(params.contains("-o"), "Parameters should contain '-o'")
            assertTrue(params.contains("ci.skip"), "Parameters should contain 'ci.skip'")
            
            mockResult
        }
        
        // Set trackInfo using reflection
        setTrackInfo(remoteName)
        setRepository(mockRepository)
        
        // When
        val result = invokePushMethod(mockRepository, pushOptions, remoteBranch)
        
        // Then
        verify(exactly = 1) { mockGit.runCommand(any<() -> GitLineHandler>()) }
        assertEquals(mockResult, result)
    }

    @Test
    fun `test push method with set-upstream when no remote configured`() {
        // Given
        val remoteBranch = "new-branch"
        val pushOptions = listOf("--force")
        val remoteUrl = "git@github.com:test/repo.git"
        
        // Setup mocks - no remote in trackInfo
        every { mockRepository.remotes } returns listOf(mockRemote)
        every { mockRemote.firstUrl } returns remoteUrl
        
        // Capture the handler
        val handlerSlot = slot<() -> GitLineHandler>()
        every { mockGit.runCommand(capture(handlerSlot)) } answers {
            val handler = handlerSlot.captured.invoke()
            
            // Verify handler setup
            assertEquals(GitCommand.PUSH, handler.command())
            
            // Verify parameters contain --set-upstream
            val params = handler.parameters()
            assertTrue(params.contains("--set-upstream"), "Parameters should contain '--set-upstream'")
            assertTrue(params.contains("origin"), "Parameters should contain 'origin'")
            assertTrue(params.contains("HEAD:$remoteBranch"), "Parameters should contain 'HEAD:$remoteBranch'")
            assertTrue(params.contains("--force"), "Parameters should contain '--force'")
            
            mockResult
        }
        
        // Don't set trackInfo (it will be null)
        setRepository(mockRepository)
        
        // When
        val result = invokePushMethod(mockRepository, pushOptions, remoteBranch)
        
        // Then
        verify(exactly = 1) { mockGit.runCommand(any<() -> GitLineHandler>()) }
        assertEquals(mockResult, result)
    }

    @Test
    fun `test push method with multiple push options`() {
        // Given
        val remoteBranch = "main"
        val pushOptions = listOf(
            "--force-with-lease",
            "-o ci.skip",
            "-o merge_request.create",
            "--verbose"
        )
        val remoteName = "origin"
        
        // Setup mocks
        every { mockRemote.name } returns remoteName
        every { mockRemote.firstUrl } returns "git@github.com:test/repo.git"
        every { mockRepository.remotes } returns listOf(mockRemote)
        
        // Capture the handler
        val handlerSlot = slot<() -> GitLineHandler>()
        every { mockGit.runCommand(capture(handlerSlot)) } answers {
            val handler = handlerSlot.captured.invoke()
            
            // Verify all push options are included
            val params = handler.parameters()
            assertTrue(params.contains("--force-with-lease"), "Should contain --force-with-lease")
            assertTrue(params.contains("-o"), "Should contain -o")
            assertTrue(params.contains("ci.skip"), "Should contain ci.skip")
            assertTrue(params.contains("merge_request.create"), "Should contain merge_request.create")
            assertTrue(params.contains("--verbose"), "Should contain --verbose")
            
            mockResult
        }
        
        setTrackInfo(remoteName)
        setRepository(mockRepository)
        
        // When
        val result = invokePushMethod(mockRepository, pushOptions, remoteBranch)
        
        // Then
        verify(exactly = 1) { mockGit.runCommand(any<() -> GitLineHandler>()) }
        assertEquals(mockResult, result)
    }

    @Test
    fun `test push method with empty push options`() {
        // Given
        val remoteBranch = "develop"
        val pushOptions = emptyList<String>()
        val remoteName = "origin"
        
        // Setup mocks
        every { mockRemote.name } returns remoteName
        every { mockRemote.firstUrl } returns "git@github.com:test/repo.git"
        every { mockRepository.remotes } returns listOf(mockRemote)
        
        // Capture the handler
        val handlerSlot = slot<() -> GitLineHandler>()
        every { mockGit.runCommand(capture(handlerSlot)) } answers {
            val handler = handlerSlot.captured.invoke()
            
            // Verify basic parameters are present
            val params = handler.parameters()
            assertTrue(params.contains(remoteName), "Should contain remote name")
            assertTrue(params.contains("HEAD:$remoteBranch"), "Should contain HEAD:$remoteBranch")
            assertTrue(params.contains("--progress"), "Should contain --progress")
            
            mockResult
        }
        
        setTrackInfo(remoteName)
        setRepository(mockRepository)
        
        // When
        val result = invokePushMethod(mockRepository, pushOptions, remoteBranch)
        
        // Then
        verify(exactly = 1) { mockGit.runCommand(any<() -> GitLineHandler>()) }
        assertEquals(mockResult, result)
    }

    @Test
    fun `test push method with complex push option string`() {
        // Given
        val remoteBranch = "feature/complex"
        val pushOptions = listOf("-o merge_request.create -o merge_request.title=\"My MR\"")
        val remoteName = "origin"
        
        // Setup mocks
        every { mockRemote.name } returns remoteName
        every { mockRemote.firstUrl } returns "git@github.com:test/repo.git"
        every { mockRepository.remotes } returns listOf(mockRemote)
        
        // Capture the handler
        val handlerSlot = slot<() -> GitLineHandler>()
        every { mockGit.runCommand(capture(handlerSlot)) } answers {
            val handler = handlerSlot.captured.invoke()
            
            // Verify parsed options are included
            val params = handler.parameters()
            assertTrue(params.contains("-o"), "Should contain -o flag")
            assertTrue(params.contains("merge_request.create"), "Should contain merge_request.create")
            assertTrue(params.contains("merge_request.title=My MR"), "Should contain parsed title")
            
            mockResult
        }
        
        setTrackInfo(remoteName)
        setRepository(mockRepository)
        
        // When
        val result = invokePushMethod(mockRepository, pushOptions, remoteBranch)
        
        // Then
        verify(exactly = 1) { mockGit.runCommand(any<() -> GitLineHandler>()) }
        assertEquals(mockResult, result)
    }

    // Helper methods using reflection to access private fields and methods
    
    private fun setTrackInfo(remoteName: String) {
        val trackInfoField = action::class.java.getDeclaredField("trackInfo")
        trackInfoField.isAccessible = true
        
        val mockTrackInfo = mockk<git4idea.repo.GitBranchTrackInfo>()
        every { mockTrackInfo.remote } returns mockRemote
        
        trackInfoField.set(action, mockTrackInfo)
    }
    
    private fun setRepository(repository: GitRepository) {
        val repositoryField = action::class.java.getDeclaredField("repository")
        repositoryField.isAccessible = true
        repositoryField.set(action, repository)
    }
    
    private fun invokePushMethod(
        repository: GitRepository,
        pushOptions: List<String>,
        remoteBranch: String
    ): GitCommandResult {
        val pushMethod = action::class.java.getDeclaredMethod(
            "push",
            GitRepository::class.java,
            List::class.java,
            String::class.java
        )
        pushMethod.isAccessible = true
        return pushMethod.invoke(action, repository, pushOptions, remoteBranch) as GitCommandResult
    }
}
