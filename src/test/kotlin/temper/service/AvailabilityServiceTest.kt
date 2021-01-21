package temper.service

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import temper.constants.ErrorMessages
import temper.entity.Reservation
import temper.exception.InvalidInputException
import temper.repository.ReservationRepository
import temper.utils.DateUtil
import temper.utils.InputValidator
import java.time.LocalDate
import kotlin.streams.toList

internal class AvailabilityServiceTest
{
    @Mock
    private val reservationRepositoryMock: ReservationRepository = mock()

    private val inputValidator = InputValidator(DateUtil())
    private val availabilityService = AvailabilityService(reservationRepositoryMock, inputValidator)

    private val ARRIVAL_DATE = LocalDate.now().plusDays(1).toString()
    private val DEPARTURE_DATE = LocalDate.now().plusDays(4).toString()

    @Test
    fun canListAvailableDateWithNoFreeSpots()
    {
        val existingReservations = listOf(Reservation(null, LocalDate.now().plusDays(1), "", ""),
                Reservation(null, LocalDate.now().plusDays(2), "", ""),
                Reservation(null, LocalDate.now().plusDays(3), "", ""))

        val arrivalDate = LocalDate.parse(ARRIVAL_DATE)
        val departureDate = LocalDate.parse(DEPARTURE_DATE)

        whenever(reservationRepositoryMock.findByReservationDateIn(arrivalDate.datesUntil(departureDate).toList())).thenReturn(existingReservations)
        val dateList = availabilityService.listAvailableDates(ARRIVAL_DATE, DEPARTURE_DATE)

        assertTrue(dateList.isEmpty())
    }

    @Test
    fun canListAvailableDate()
    {
        val existingReservations = listOf(Reservation(null, LocalDate.now().plusDays(1), "", ""),
                Reservation(null, LocalDate.now().plusDays(2), "", ""))

        val arrivalDate = LocalDate.parse(ARRIVAL_DATE)
        val departureDate = LocalDate.parse(DEPARTURE_DATE)
        whenever(reservationRepositoryMock.findByReservationDateIn(arrivalDate.datesUntil(departureDate).toList())).thenReturn(existingReservations)
        val dateList = availabilityService.listAvailableDates(ARRIVAL_DATE, DEPARTURE_DATE)

        assertEquals(dateList.joinToString(), LocalDate.now().plusDays(3).toString())
    }

    @Test
    fun canListAvailableDateWithNoReservations()
    {
        val arrivalDate = LocalDate.parse(ARRIVAL_DATE)
        val departureDate = LocalDate.parse(DEPARTURE_DATE)
        whenever(reservationRepositoryMock.findByReservationDateIn(arrivalDate.datesUntil(departureDate).toList())).thenReturn(listOf())
        val dateList = availabilityService.listAvailableDates(ARRIVAL_DATE, DEPARTURE_DATE)

        assertEquals(dateList.joinToString(), arrivalDate.datesUntil(departureDate).toList().joinToString())
    }

    @Test
    fun cannotListAvailableDatesWithNullDepartureDate()
    {
        val exception = assertThrows<InvalidInputException> {
            availabilityService.listAvailableDates(null, ARRIVAL_DATE)
        }

        assertEquals(exception.message, ErrorMessages.MALFORMED_DATE_PARAMETER.message)
    }

    @Test
    fun cannotListAvailableDatesWithInvalidArrivalDate()
    {
        val exception = assertThrows<InvalidInputException> {
            availabilityService.listAvailableDates(ARRIVAL_DATE, "1970-01-O1")
        }

        assertEquals(exception.message, ErrorMessages.MALFORMED_DATE_PARAMETER.message)
    }

    @Test
    fun cannotListAvailableWithDateInThePast()
    {
        val exception = assertThrows<InvalidInputException> {
            availabilityService.listAvailableDates(LocalDate.now().minusDays(1).toString(), ARRIVAL_DATE)
        }

        assertEquals(exception.message, ErrorMessages.DATE_IN_THE_PAST.message)
    }

    @Test
    fun cannotListAvailableDepartureTooSoon()
    {
        val exception = assertThrows<InvalidInputException> {
            availabilityService.listAvailableDates(DEPARTURE_DATE, ARRIVAL_DATE)
        }

        assertEquals(exception.message, ErrorMessages.DEPARTURE_DATE_TOO_SOON.message)
    }

    @Test
    fun isDateRangeAvailableWithNoFreeDates()
    {
        val existingReservations = listOf(Reservation(null, LocalDate.now().plusDays(1), "", ""),
                Reservation(null, LocalDate.now().plusDays(2), "", ""),
                Reservation(null, LocalDate.now().plusDays(3), "", ""))

        val arrivalDate = LocalDate.parse(ARRIVAL_DATE)
        val departureDate = LocalDate.parse(DEPARTURE_DATE)

        whenever(reservationRepositoryMock.findByReservationDateIn(arrivalDate.datesUntil(departureDate).toList())).thenReturn(existingReservations)
        assertFalse(availabilityService.isDateRangeAvailable(ARRIVAL_DATE, DEPARTURE_DATE))
    }

    @Test
    fun isDateRangeAvailableWithOneFreeDates()
    {
        val existingReservations = listOf(Reservation(null, LocalDate.now().plusDays(1), "", ""),
                Reservation(null, LocalDate.now().plusDays(2), "", ""))

        val arrivalDate = LocalDate.parse(ARRIVAL_DATE)
        val departureDate = LocalDate.parse(DEPARTURE_DATE)

        whenever(reservationRepositoryMock.findByReservationDateIn(arrivalDate.datesUntil(departureDate).toList())).thenReturn(existingReservations)
        assertFalse(availabilityService.isDateRangeAvailable(ARRIVAL_DATE, DEPARTURE_DATE))
    }

    @Test
    fun isDateRangeAvailableWithAllDateFree()
    {
        val arrivalDate = LocalDate.parse(ARRIVAL_DATE)
        val departureDate = LocalDate.parse(DEPARTURE_DATE)

        whenever(reservationRepositoryMock.findByReservationDateIn(arrivalDate.datesUntil(departureDate).toList())).thenReturn(listOf())
        assertTrue(availabilityService.isDateRangeAvailable(ARRIVAL_DATE, DEPARTURE_DATE))
    }
}