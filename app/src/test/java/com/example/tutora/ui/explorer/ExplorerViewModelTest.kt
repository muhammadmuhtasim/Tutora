package com.example.tutora.ui.explorer

import app.cash.turbine.test
import com.example.tutora.domain.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExplorerViewModelTest {

    private val authRepo = mockk<AuthRepository>()
    private val postRepo = mockk<PostRepository>()
    private val bookingRepo = mockk<BookingRepository>()
    private lateinit var viewModel: ExplorerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        
        // Default mocks
        coEvery { authRepo.getCurrentUser() } returns AppResult.Success(
            User(id = "1", name = "Test", role = UserRole.STUDENT, location = GeoPoint(0.0, 0.0), region = "Test")
        )
        coEvery { postRepo.getPosts(any(), any(), any(), any(), any()) } returns flowOf(emptyList())
        coEvery { postRepo.getPostsByIds(any()) } returns flowOf(emptyList())
        
        viewModel = ExplorerViewModel(authRepo, postRepo)
    }

    @Test
    fun `test initial state reflects user role`() = kotlinx.coroutines.test.runTest {
        viewModel.uiState.test {
            assertEquals(UserRole.STUDENT, awaitItem().userRole)
        }
    }
}
