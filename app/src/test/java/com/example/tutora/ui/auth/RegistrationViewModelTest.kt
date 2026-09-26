package com.example.tutora.ui.auth

import app.cash.turbine.test
import com.example.tutora.domain.AuthRepository
import com.example.tutora.domain.LocationRepository
import com.example.tutora.domain.UserRole
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationViewModelTest {

    private val authRepo = mockk<AuthRepository>()
    private val locationRepo = mockk<LocationRepository>()
    private lateinit var viewModel: RegistrationViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = RegistrationViewModel(authRepo, locationRepo)
    }

    @Test
    fun `test step navigation with validation`() = kotlinx.coroutines.test.runTest {
        assertEquals(1, viewModel.uiState.value.step)
        
        // Should fail step 1 validation if name is empty
        viewModel.nextStep()
        assertEquals(1, viewModel.uiState.value.step)
        assertEquals("Please enter your name", viewModel.uiState.value.error)

        viewModel.onNameChanged("Test User")
        viewModel.onPhoneChanged("1234567890")
        viewModel.nextStep()
        assertEquals(2, viewModel.uiState.value.step)

        // Should fail step 2 validation if location is not selected
        viewModel.nextStep()
        assertEquals(2, viewModel.uiState.value.step)
        assertEquals("Please select a location from search suggestions or use GPS", viewModel.uiState.value.error)

        viewModel.onLocationSelected(com.example.tutora.domain.LocationResult("Address", com.example.tutora.domain.GeoPoint()), advance = true)
        assertEquals(3, viewModel.uiState.value.step)
        
        viewModel.prevStep()
        assertEquals(2, viewModel.uiState.value.step)
    }

    @Test
    fun `test role change`() = kotlinx.coroutines.test.runTest {
        viewModel.uiState.test {
            assertEquals(UserRole.STUDENT, awaitItem().role)
            
            viewModel.onRoleChanged(UserRole.TUTOR)
            assertEquals(UserRole.TUTOR, awaitItem().role)
        }
    }

    @Test
    fun `register should fail if email is invalid`() = kotlinx.coroutines.test.runTest {
        viewModel.onNameChanged("Test User")
        viewModel.onPhoneChanged("1234567890")
        viewModel.onEmailChanged("invalid-email")
        viewModel.onPasswordChanged("123456")
        // Note: selectedLocation must be set too
        viewModel.onLocationSelected(com.example.tutora.domain.LocationResult("Address", com.example.tutora.domain.GeoPoint()))
        
        viewModel.register()
        
        viewModel.uiState.test {
            assertEquals("Please enter a valid email address", awaitItem().error)
        }
    }
}
