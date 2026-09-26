package com.example.tutora.domain

sealed interface BookingState {
    data object Pending : BookingState
    data object Accepted : BookingState
    data object Preparing : BookingState
    data object OnTheWay : BookingState
    data object Arrived : BookingState
    data object Rejected : BookingState // terminal
    data object Completed : BookingState // terminal
    data object Cancelled : BookingState // terminal
}

sealed interface BookingEvent {
    data object Accept : BookingEvent
    data object Reject : BookingEvent
    data object StartPreparing : BookingEvent
    data object StartJourney : BookingEvent
    data object Arrive : BookingEvent
    data object Complete : BookingEvent
    data object Cancel : BookingEvent
}

class BookingStateEngine {
    fun transition(current: BookingState, event: BookingEvent): Result<BookingState> =
        when (current) {
            BookingState.Pending -> when (event) {
                BookingEvent.Accept -> Result.success(BookingState.Accepted)
                BookingEvent.Reject -> Result.success(BookingState.Rejected)
                BookingEvent.Cancel -> Result.success(BookingState.Cancelled)
                else -> Result.failure(IllegalStateException("Invalid event $event for PENDING"))
            }
            BookingState.Accepted -> when (event) {
                BookingEvent.StartPreparing -> Result.success(BookingState.Preparing)
                BookingEvent.Cancel -> Result.success(BookingState.Cancelled)
                else -> Result.failure(IllegalStateException("Invalid event $event for ACCEPTED"))
            }
            BookingState.Preparing -> when (event) {
                BookingEvent.StartJourney -> Result.success(BookingState.OnTheWay)
                BookingEvent.Cancel -> Result.success(BookingState.Cancelled)
                else -> Result.failure(IllegalStateException("Invalid event $event for PREPARING"))
            }
            BookingState.OnTheWay -> when (event) {
                BookingEvent.Arrive -> Result.success(BookingState.Arrived)
                BookingEvent.Cancel -> Result.success(BookingState.Cancelled)
                else -> Result.failure(IllegalStateException("Invalid event $event for ON_THE_WAY"))
            }
            BookingState.Arrived -> when (event) {
                BookingEvent.Complete -> Result.success(BookingState.Completed)
                BookingEvent.Cancel -> Result.success(BookingState.Cancelled)
                else -> Result.failure(IllegalStateException("Invalid event $event for ARRIVED"))
            }
            BookingState.Rejected, BookingState.Completed, BookingState.Cancelled ->
                Result.failure(IllegalStateException("Terminal state ${current.javaClass.simpleName}: no further transitions"))
        }
}
