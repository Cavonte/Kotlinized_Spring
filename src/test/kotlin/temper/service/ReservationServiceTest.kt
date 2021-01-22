package temper.service

import com.nhaarman.mockito_kotlin.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import temper.constants.ErrorMessages
import temper.constants.ReservationConstraints
import temper.entity.Reservation
import temper.exception.InvalidInputException
import temper.repository.ReservationRepository
import temper.utils.DateUtil
import temper.utils.InputValidator
import temper.utils.TestUtils
import java.time.LocalDate

internal class ReservationServiceTest
{
    private val reservationRepositoryMock: ReservationRepository = mock()

    private val availabilityServiceMock: AvailabilityService = mock()
    private val inputValidator = InputValidator(DateUtil())
    private val testUtil: TestUtils = TestUtils()
    private val reservationService = ReservationService(availabilityServiceMock, inputValidator, reservationRepositoryMock, testUtil)

    private val FIRST_NAME = "Kick Buttowski"
    private val LAST_NAME = "Suburban"
    private val EMAIL = "Daredevil@email.com"
    private val ARRIVAL_DATE = LocalDate.now().plusDays(1).toString()
    private val DEPARTURE_DATE = LocalDate.now().plusDays(3).toString()
    private val BOOKING_IDENTIFIER = "dedicated_wham@email.com_1234567"

    @Test
    fun bookReservation()
    {
        whenever(availabilityServiceMock.isDateRangeAvailable(any(), any())).thenReturn(true)

        val id = reservationService.bookReservation(EMAIL, FIRST_NAME, LAST_NAME, ARRIVAL_DATE, DEPARTURE_DATE)

        val captor = argumentCaptor<MutableList<Reservation>>()
        verify(availabilityServiceMock).isDateRangeAvailable(ARRIVAL_DATE, DEPARTURE_DATE)
        verify(reservationRepositoryMock).saveAll(captor.capture())
        assertEquals(captor.firstValue.size, 2)
        assertTrue(Regex("""${LAST_NAME}_${EMAIL}_""").containsMatchIn(id))
    }

    @Test
    fun cannotBookReservationWithUnavailableDays()
    {
        whenever(availabilityServiceMock.isDateRangeAvailable(ARRIVAL_DATE, DEPARTURE_DATE)).thenReturn(false)
        val exception = assertThrows<InvalidInputException> {
            reservationService.bookReservation(EMAIL, FIRST_NAME, LAST_NAME, ARRIVAL_DATE, DEPARTURE_DATE)
        }

        assertEquals(exception.message, ErrorMessages.UNAVAILABLE_RESERVATION_DATES.message)
    }

    @Test
    fun cannotCreateBookingWithNullEmail()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.bookReservation(null, FIRST_NAME, LAST_NAME, ARRIVAL_DATE, DEPARTURE_DATE)
        }

        assertEquals(exception.message, ErrorMessages.MALFORMED_EMAIL.message)
    }

    @Test
    fun cannotCreateBookingWithInvalidEmail()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.bookReservation("$$$@lol@", FIRST_NAME, LAST_NAME, ARRIVAL_DATE, DEPARTURE_DATE)
        }

        assertEquals(exception.message, ErrorMessages.MALFORMED_EMAIL.message)
    }

    @Test
    fun cannotCreateBookingWithNullName()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.bookReservation(EMAIL, null, LAST_NAME, ARRIVAL_DATE, DEPARTURE_DATE)
        }

        assertEquals(exception.message, ErrorMessages.MALFORMED_FIRST_NAME.message)
    }

    @Test
    fun cannotCreateBookingWithInvalidLastName()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.bookReservation(EMAIL, FIRST_NAME, "111111", ARRIVAL_DATE, DEPARTURE_DATE)
        }

        assertEquals(exception.message, ErrorMessages.MALFORMED_LAST_NAME.message)
    }

    @Test
    fun cannotCreateBookingWithNullDepartureDate()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.bookReservation(EMAIL, FIRST_NAME, LAST_NAME, ARRIVAL_DATE, null)
        }

        assertEquals(exception.message, ErrorMessages.MALFORMED_DEPARTURE_DATE.message)
    }

    @Test
    fun cannotCreateBookingWithInvalidArrivalDate()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.bookReservation(EMAIL, FIRST_NAME, LAST_NAME, "1970-O1-O1", null)
        }

        assertEquals(exception.message, ErrorMessages.MALFORMED_ARRIVAL_DATE.message)
    }

    @Test
    fun cannotBookReservationInThePast()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.bookReservation(EMAIL, FIRST_NAME, LAST_NAME, "1970-01-01", DEPARTURE_DATE)
        }

        assertEquals(exception.message, ErrorMessages.DATE_IN_THE_PAST.message)
    }

    @Test
    fun cannotBookReservationPastThreshold()
    {
        val faultyArrivalDate = LocalDate.now().plusDays((ReservationConstraints.MAX_ADVANCE_RESERVATION_DATE.days + 1).toLong()).toString()
        val faultyDepartureDate = LocalDate.now().plusDays((ReservationConstraints.MAX_ADVANCE_RESERVATION_DATE.days + 3).toLong()).toString()

        val exception = assertThrows<InvalidInputException> {
            reservationService.bookReservation(EMAIL, FIRST_NAME, LAST_NAME, faultyArrivalDate, faultyDepartureDate)
        }

        assertEquals(exception.message, ErrorMessages.DATE_TOO_FAR.message)
    }

    @Test
    fun cannotBookReservationTooSoon()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.bookReservation(EMAIL, FIRST_NAME, LAST_NAME, LocalDate.now().toString(), DEPARTURE_DATE)
        }

        assertEquals(exception.message, ErrorMessages.DATE_TOO_SOON.message)
    }

    @Test
    fun cannotBookReservationWithDepartureDateLessThanArrivalDate()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.bookReservation(EMAIL, FIRST_NAME, LAST_NAME, DEPARTURE_DATE, ARRIVAL_DATE)
        }

        assertEquals(exception.message, ErrorMessages.DEPARTURE_DATE_TOO_SOON.message)
    }

    @Test
    fun cannotReserveMoreDaysThanLimit()
    {
        val faultyArrivalDate = LocalDate.now().plusDays(1).toString()
        val faultyDepartureDate = LocalDate.now().plusDays(6).toString()

        val exception = assertThrows<InvalidInputException> {
            reservationService.bookReservation(EMAIL, FIRST_NAME, LAST_NAME, faultyArrivalDate, faultyDepartureDate)
        }

        assertEquals(exception.message, ErrorMessages.RESERVATION_LIMIT_REACHED.message)
    }


    @Test
    fun canModifySharedDatesReservation()
    {
        val oldArrival = LocalDate.now().plusDays(1)
        val oldDeparture = LocalDate.now().plusDays(3)
        val newArrival = LocalDate.now().plusDays(2)
        val newDeparture = LocalDate.now().plusDays(5)

        val existingReservations = getReservationList(oldArrival.toString(),
                oldDeparture.toString(),
                EMAIL,
                BOOKING_IDENTIFIER)

        val existingReservationDates = existingReservations.map { reservation -> reservation.reservationDate }

        val newReservations = getReservationList(newArrival.toString(),
                newDeparture.toString(),
                EMAIL,
                BOOKING_IDENTIFIER)

        val newReservationsDates = newReservations.map { reservation -> reservation.reservationDate }

        whenever(reservationRepositoryMock.existsByBookingIdentifier(BOOKING_IDENTIFIER)).thenReturn(true)
        whenever(reservationRepositoryMock.findByBookingIdentifier(BOOKING_IDENTIFIER)).thenReturn(existingReservations)
        whenever(reservationRepositoryMock.existsByReservationDateIn(newReservationsDates)).thenReturn(false)

        reservationService.modifyReservation(EMAIL, newArrival.toString(), newDeparture.toString(), BOOKING_IDENTIFIER)

        verify(reservationRepositoryMock).existsByBookingIdentifier(BOOKING_IDENTIFIER)
        verify(reservationRepositoryMock).findByBookingIdentifier(BOOKING_IDENTIFIER)
        verify(reservationRepositoryMock).existsByReservationDateIn(newReservationsDates.subtract(existingReservationDates).toList())
        verify(reservationRepositoryMock).saveAll(newReservations.subtract(existingReservations).toList())
        verify(reservationRepositoryMock).deleteByReservationDateIn(existingReservationDates.subtract(newReservationsDates))
        verifyNoMoreInteractions(reservationRepositoryMock)
    }

    @Test
    fun canModifySeparatedDatesReservations()
    {
        val oldArrival = LocalDate.now().plusDays(1)
        val oldDeparture = LocalDate.now().plusDays(2)
        val newArrival = LocalDate.now().plusDays(4)
        val newDeparture = LocalDate.now().plusDays(7)

        val existingReservations = getReservationList(oldArrival.toString(),
                oldDeparture.toString(),
                EMAIL,
                BOOKING_IDENTIFIER)

        val existingReservationDates = existingReservations.map { reservation -> reservation.reservationDate }

        val newReservations = getReservationList(newArrival.toString(),
                newDeparture.toString(),
                EMAIL,
                BOOKING_IDENTIFIER)

        val newReservationsDates = newReservations.map { reservation -> reservation.reservationDate }

        whenever(reservationRepositoryMock.existsByBookingIdentifier(BOOKING_IDENTIFIER)).thenReturn(true)
        whenever(reservationRepositoryMock.findByBookingIdentifier(BOOKING_IDENTIFIER)).thenReturn(existingReservations)
        whenever(reservationRepositoryMock.existsByReservationDateIn(newReservationsDates)).thenReturn(false)

        reservationService.modifyReservation(EMAIL, newArrival.toString(), newDeparture.toString(), BOOKING_IDENTIFIER)

        verify(reservationRepositoryMock).existsByBookingIdentifier(BOOKING_IDENTIFIER)
        verify(reservationRepositoryMock).findByBookingIdentifier(BOOKING_IDENTIFIER)
        verify(reservationRepositoryMock).existsByReservationDateIn(newReservationsDates.subtract(existingReservationDates).toList())
        verify(reservationRepositoryMock).saveAll(newReservations.subtract(existingReservations).toList())
        verify(reservationRepositoryMock).deleteByReservationDateIn(existingReservationDates.subtract(newReservationsDates))
        verifyNoMoreInteractions(reservationRepositoryMock)
    }

    @Test
    fun cannotModifyWithTakenDateRange()
    {
        val oldArrival = LocalDate.now().plusDays(1)
        val oldDeparture = LocalDate.now().plusDays(2)
        val newArrival = LocalDate.now().plusDays(2)
        val newDeparture = LocalDate.now().plusDays(5)

        val existingReservations = getReservationList(oldArrival.toString(),
                oldDeparture.toString(),
                EMAIL,
                BOOKING_IDENTIFIER)

        val newReservations = getReservationList(newArrival.toString(),
                newDeparture.toString(),
                EMAIL,
                BOOKING_IDENTIFIER)

        val newReservationsDates = newReservations.map { reservation -> reservation.reservationDate }

        whenever(reservationRepositoryMock.existsByBookingIdentifier(BOOKING_IDENTIFIER)).thenReturn(true)
        whenever(reservationRepositoryMock.findByBookingIdentifier(BOOKING_IDENTIFIER)).thenReturn(existingReservations)
        whenever(reservationRepositoryMock.existsByReservationDateIn(newReservationsDates)).thenReturn(true)

        val exception = assertThrows<InvalidInputException> {
            reservationService.modifyReservation(EMAIL, newArrival.toString(), newDeparture.toString(), BOOKING_IDENTIFIER)
        }

        assertEquals(exception.message, ErrorMessages.UNAVAILABLE_RESERVATION_DATES.message)

        verify(reservationRepositoryMock).existsByBookingIdentifier(BOOKING_IDENTIFIER)
        verify(reservationRepositoryMock).findByBookingIdentifier(BOOKING_IDENTIFIER)
        verify(reservationRepositoryMock).existsByReservationDateIn(newReservationsDates)
        verifyNoMoreInteractions(reservationRepositoryMock)
    }

    @Test
    fun cannotModifyNullReservation()
    {
        whenever(reservationRepositoryMock.existsByBookingIdentifier(BOOKING_IDENTIFIER)).thenReturn(false)
        val exception = assertThrows<InvalidInputException> {
            reservationService.modifyReservation(EMAIL, ARRIVAL_DATE, DEPARTURE_DATE, BOOKING_IDENTIFIER)
        }

        assertEquals(exception.message, ErrorMessages.RESERVATION_DOES_NOT_EXIST.message)
        verify(reservationRepositoryMock).existsByBookingIdentifier(BOOKING_IDENTIFIER)
        verifyNoMoreInteractions(reservationRepositoryMock)
    }

    @Test
    fun cannotModifyBookingWithNullArrivalDate()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.modifyReservation(EMAIL, null, DEPARTURE_DATE, BOOKING_IDENTIFIER)
        }

        assertEquals(exception.message, ErrorMessages.MALFORMED_ARRIVAL_DATE.message)
    }

    @Test
    fun cannotModifyBookingWithInvalidDepartureDate()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.modifyReservation(EMAIL, ARRIVAL_DATE, "1903-O9-o2", BOOKING_IDENTIFIER)
        }

        assertEquals(exception.message, ErrorMessages.MALFORMED_DEPARTURE_DATE.message)
    }

    @Test
    fun cannotModifyBookingWithNullEmail()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.modifyReservation(null, ARRIVAL_DATE, DEPARTURE_DATE, null)
        }

        assertEquals(exception.message, ErrorMessages.MALFORMED_EMAIL.message)
    }

    @Test
    fun cannotModifyBookingWithInvalidEmail()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.modifyReservation("""some##aol.com""", ARRIVAL_DATE, DEPARTURE_DATE, null)
        }

        assertEquals(exception.message, ErrorMessages.MALFORMED_EMAIL.message)
    }

    @Test
    fun cannotModifyBookingWithNullBookingIdentifier()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.modifyReservation(EMAIL, ARRIVAL_DATE, DEPARTURE_DATE, null)
        }

        assertEquals(exception.message, ErrorMessages.MALFORMED_BOOKING_IDENTIFIER.message)
    }

    @Test
    fun cannotModifyBookingWithInvalidBookingIdentifier()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.modifyReservation(EMAIL, ARRIVAL_DATE, DEPARTURE_DATE, null)
        }

        assertEquals(exception.message, ErrorMessages.MALFORMED_BOOKING_IDENTIFIER.message)
    }


    @Test
    fun cancelReservation()
    {
        val reservationList = getReservationList("1970-01-01", "1970-01-04", EMAIL, BOOKING_IDENTIFIER)

        whenever(reservationRepositoryMock.existsByBookingIdentifier(BOOKING_IDENTIFIER)).thenReturn(true)
        whenever(reservationRepositoryMock.findByBookingIdentifier(BOOKING_IDENTIFIER)).thenReturn(reservationList)
        reservationService.cancelReservation(EMAIL, BOOKING_IDENTIFIER)

        verify(reservationRepositoryMock).existsByBookingIdentifier(BOOKING_IDENTIFIER)
        verify(reservationRepositoryMock).findByBookingIdentifier(BOOKING_IDENTIFIER)
        verify(reservationRepositoryMock).deleteAll(reservationList)
    }

    @Test
    fun cannotCancelWithWrongEmail()
    {
        val reservationList = getReservationList("1970-01-01", "1970-01-04", EMAIL, BOOKING_IDENTIFIER)

        whenever(reservationRepositoryMock.existsByBookingIdentifier(BOOKING_IDENTIFIER)).thenReturn(true)
        whenever(reservationRepositoryMock.findByBookingIdentifier(BOOKING_IDENTIFIER)).thenReturn(reservationList)

        val exception = assertThrows<InvalidInputException> {
            reservationService.cancelReservation("yoko@aol.com", BOOKING_IDENTIFIER)
        }

        assertEquals(exception.message, ErrorMessages.RESERVATION_DOES_NOT_EXIST.message)
    }

    @Test
    fun cannotCancelNullReservation()
    {
        whenever(reservationRepositoryMock.existsByBookingIdentifier(BOOKING_IDENTIFIER)).thenReturn(false)
        val exception = assertThrows<InvalidInputException> {
            reservationService.cancelReservation(EMAIL, BOOKING_IDENTIFIER)
        }

        assertEquals(exception.message, ErrorMessages.RESERVATION_DOES_NOT_EXIST.message)

    }

    private fun getReservationList(starDate: String, endDate: String, email: String, bookingIdentifier: String): List<Reservation>
    {
        val reservationList: MutableList<Reservation> = mutableListOf()

        LocalDate.parse(starDate).datesUntil(LocalDate.parse(endDate)).forEach { date ->
            reservationList.add(Reservation(null, date, bookingIdentifier, email))
        }

        return reservationList
    }

    @Test
    fun cannotCancelBookingWithNullEmail()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.cancelReservation(null, BOOKING_IDENTIFIER)
        }

        assertEquals(exception.message, ErrorMessages.MALFORMED_EMAIL.message)
    }

    @Test
    fun cannotCancelBookingWithInvalidEmail()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.cancelReservation("b@@@@@mail.com", BOOKING_IDENTIFIER)
        }

        assertEquals(exception.message, ErrorMessages.MALFORMED_EMAIL.message)
    }

    @Test
    fun cannotCancelBookingWithNullBookingIdentifier()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.cancelReservation(EMAIL, null)
        }

        assertEquals(exception.message, ErrorMessages.MALFORMED_BOOKING_IDENTIFIER.message)
    }

    @Test
    fun cannotCancelBookingWithInvalidBookingIdentifier()
    {
        val exception = assertThrows<InvalidInputException> {
            reservationService.cancelReservation(EMAIL, "nope__someEmail@email.com__1234567")
        }

        assertEquals(exception.message, ErrorMessages.MALFORMED_BOOKING_IDENTIFIER.message)
    }
}