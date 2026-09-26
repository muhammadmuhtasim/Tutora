package com.example.tutora

import app.cash.turbine.test
import com.example.tutora.domain.*
import com.example.tutora.ui.bookings.BookingFlowViewModel
import com.example.tutora.ui.explorer.ExplorerFilters
import com.example.tutora.ui.explorer.ExplorerViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ComprehensiveTests {

    private val authRepo = mockk<AuthRepository>()
    private val postRepo = mockk<PostRepository>()
    private val bookingRepo = mockk<BookingRepository>()
    private val chatRepo = mockk<ChatRepository>()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `Point 1 & 6 - Auth and Data Flow Test`() = runTest {
        val user = User(id = "123", name = "Test User", role = UserRole.STUDENT)
        coEvery { authRepo.getCurrentUser() } returns AppResult.Success(user)
        coEvery { postRepo.getPosts(any(), any(), any(), any(), any()) } returns flowOf(emptyList())
        coEvery { postRepo.getPostsByIds(any()) } returns flowOf(emptyList())

        val viewModel = ExplorerViewModel(authRepo, postRepo)
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(UserRole.STUDENT, state.userRole)
        }
    }

    @Test
    fun `Point 4 - Filter Logic Test`() = runTest {
        val user = User(id = "123", role = UserRole.STUDENT)
        coEvery { authRepo.getCurrentUser() } returns AppResult.Success(user)
        coEvery { postRepo.getPosts(any(), any(), any(), any(), any()) } returns flowOf(emptyList())
        coEvery { postRepo.getPostsByIds(any()) } returns flowOf(emptyList())

        val viewModel = ExplorerViewModel(authRepo, postRepo)
        val filters = ExplorerFilters(classType = "Grade 10")
        
        viewModel.onFilterChange(filters, 5.0)
        
        viewModel.uiState.test {
            // Filter change might have happened before test block starts due to Dispatcher.Main
            // so we check the current state
            val state = awaitItem()
            assertEquals("Grade 10", state.filters.classType)
        }
    }

    @Test
    fun `Point 8 - Multi-step Booking Flow Test`() = runTest {
        val user = User(id = "student1", role = UserRole.STUDENT)
        val post = Post(id = "post1", creatorId = "tutor1")
        
        coEvery { authRepo.getCurrentUser() } returns AppResult.Success(user)
        coEvery { bookingRepo.createBooking(any()) } returns AppResult.Success(Unit)
        coEvery { chatRepo.createChatSession(any()) } returns AppResult.Success("session1")

        val viewModel = BookingFlowViewModel(authRepo, postRepo, bookingRepo, chatRepo)
        
        viewModel.uiState.test {
            awaitItem() // Skip initial state
            
            viewModel.onDateChange(1723680000000L) // Aug 15, 2026
            awaitItem()
            
            viewModel.onTimeChange(10, 0)
            awaitItem()
            
            viewModel.nextStep()
            assertEquals(2, awaitItem().step)
            
            viewModel.confirmBooking(post)
            // Skip loading states and check final success state
            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }
            
            assertEquals(3, state.step)
            assertEquals(true, state.isSuccess)
        }
    }
}
