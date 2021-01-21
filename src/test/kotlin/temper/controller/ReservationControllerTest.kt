package temper.controller

import com.google.gson.Gson
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import junit.framework.Assert.assertEquals
import org.junit.jupiter.api.Test
import temper.service.ReservationService
import java.time.LocalDate

internal class ReservationControllerTest
{
    private val reservationServiceMock: ReservationService = mock()
    private val reservationController = ReservationController(reservationServiceMock)

    private val BOOKING_ID_1 = "Smith_smith@email.com_1234151"
    private val EMAIL = "Daredevil@email.com"
    private val FIRST_NAME = "Suburban"
    private val LAST_NAME = "DaredEvil"
    private val ARRIVAL_DATE = LocalDate.now().plusDays(1).toString()
    private val DEPARTURE_DATE = LocalDate.now().plusDays(3).toString()

    private val gson = Gson()

    @Test
    fun bookReservation()
    {
        whenever(reservationServiceMock.bookReservation(EMAIL, FIRST_NAME, LAST_NAME, ARRIVAL_DATE, DEPARTURE_DATE)).thenReturn(BOOKING_ID_1)

        val bookingIdBodyActual = reservationController.bookReservation(EMAIL, FIRST_NAME, LAST_NAME, ARRIVAL_DATE, DEPARTURE_DATE)

        verify(reservationServiceMock).bookReservation(EMAIL, FIRST_NAME, LAST_NAME, ARRIVAL_DATE, DEPARTURE_DATE)
        assertEquals(bookingIdBodyActual.body, gson.toJson("BookingId: $BOOKING_ID_1"))
    }

    @Test
    fun modifyReservation()
    {
        val bookingIdBodyActual = reservationController.modifyReservation(EMAIL, ARRIVAL_DATE, DEPARTURE_DATE, BOOKING_ID_1)

        verify(reservationServiceMock).modifyReservation(EMAIL, ARRIVAL_DATE, DEPARTURE_DATE, BOOKING_ID_1)
        assertEquals(bookingIdBodyActual.body, gson.toJson("BookingId: $BOOKING_ID_1"))
    }

    @Test
    fun cancelReservation()
    {
        val bookingIdBodyActual = reservationController.cancelReservation(EMAIL, BOOKING_ID_1)

        verify(reservationServiceMock).cancelReservation(EMAIL, BOOKING_ID_1)
        assertEquals(bookingIdBodyActual.body, gson.toJson("Canceled: $BOOKING_ID_1"))
    }
}