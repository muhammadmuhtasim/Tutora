package com.example.tutora.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingStateEngineTest {

    private val engine = BookingStateEngine()

    @Test
    fun `test happy path transitions`() {
        val step1 = engine.transition(BookingState.Pending, BookingEvent.Accept)
        assertTrue(step1.isSuccess)
        assertEquals(BookingState.Accepted, step1.getOrNull())

        val step2 = engine.transition(BookingState.Accepted, BookingEvent.StartPreparing)
        assertTrue(step2.isSuccess)
        assertEquals(BookingState.Preparing, step2.getOrNull())

        val step3 = engine.transition(BookingState.Preparing, BookingEvent.StartJourney)
        assertTrue(step3.isSuccess)
        assertEquals(BookingState.OnTheWay, step3.getOrNull())

        val step4 = engine.transition(BookingState.OnTheWay, BookingEvent.Arrive)
        assertTrue(step4.isSuccess)
        assertEquals(BookingState.Arrived, step4.getOrNull())

        val step5 = engine.transition(BookingState.Arrived, BookingEvent.Complete)
        assertTrue(step5.isSuccess)
        assertEquals(BookingState.Completed, step5.getOrNull())
    }

    @Test
    fun `test cancellation from accepted state`() {
        val result = engine.transition(BookingState.Accepted, BookingEvent.Cancel)
        assertTrue(result.isSuccess)
        assertEquals(BookingState.Cancelled, result.getOrNull())
    }

    @Test
    fun `test invalid transition fails`() {
        val result = engine.transition(BookingState.Pending, BookingEvent.Complete)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `test terminal state fails`() {
        val result = engine.transition(BookingState.Completed, BookingEvent.Accept)
        assertTrue(result.isFailure)
    }
}
