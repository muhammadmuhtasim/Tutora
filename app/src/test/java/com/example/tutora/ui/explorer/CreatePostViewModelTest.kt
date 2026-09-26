package com.example.tutora.ui.explorer

import app.cash.turbine.test
import com.example.tutora.domain.*
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreatePostViewModelTest {

    private val authRepo = mockk<AuthRepository>()
    private val postRepo = mockk<PostRepository>()
    private lateinit var viewModel: CreatePostViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockkStatic(Log::class)
        coEvery { Log.d(any(), any()) } returns 0
        coEvery { Log.e(any(), any()) } returns 0
        coEvery { Log.e(any(), any(), any()) } returns 0
        
        // Mock init call
        coEvery { authRepo.getCurrentUser() } returns AppResult.Success(null)
        coEvery { postRepo.getPostsByCreatorId(any()) } returns kotlinx.coroutines.flow.flowOf(emptyList())
        
        viewModel = CreatePostViewModel(authRepo, postRepo)
    }

    private fun fillValidBasics() {
        viewModel.onClassTypeChange("Grade 10")
        viewModel.onAddSubject("Math")
        viewModel.onAddSubject("Physics")
        viewModel.onSessionLengthChange("1 hour")
        viewModel.onSessionsPerWeekChange("3")
        viewModel.onAmountChange("1000")
    }

    @Test
    fun `submitPost should fail if amount is less than 500`() = kotlinx.coroutines.test.runTest {
        fillValidBasics()
        viewModel.onAmountChange("499")
        viewModel.submitPost()
        
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals("Amount must be at least 500 BDT.", state.error)
            assertEquals(false, state.isLoading)
        }
    }

    @Test
    fun `submitPost should fail if contactInfo is empty`() = kotlinx.coroutines.test.runTest {
        coEvery { authRepo.getCurrentUser() } returns AppResult.Success(
            User(id = "1", name = "Test", role = UserRole.STUDENT, location = GeoPoint(23.0, 90.0), contactInfo = emptyList())
        )
        
        fillValidBasics()
        viewModel.submitPost()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals("Please add at least one contact info in your profile before posting.", state.error)
        }
    }

    @Test
    fun `submitPost should succeed if all validations pass`() = kotlinx.coroutines.test.runTest {
        coEvery { authRepo.getCurrentUser() } returns AppResult.Success(
            User(id = "1", name = "Test", role = UserRole.STUDENT, location = GeoPoint(23.0, 90.0), contactInfo = listOf("017..."))
        )
        coEvery { postRepo.createPost(any()) } returns AppResult.Success(Unit)

        fillValidBasics()
        viewModel.submitPost()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(true, state.isSuccess)
        }
    }
}
