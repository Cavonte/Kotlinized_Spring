package temper.controller

import com.google.gson.Gson
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import junit.framework.Assert
import org.junit.jupiter.api.Test
import temper.service.AvailabilityService
import java.time.LocalDate
import kotlin.streams.toList

internal class AvailabilityControllerTest
{
    private val availabilityServiceMock: AvailabilityService = mock()
    private val availabilityController = AvailabilityController(availabilityServiceMock)

    private val START_DATE = LocalDate.now()
    private val END_DATE = LocalDate.now().plusDays(15)
    private val DEFAULT_END_DATE = LocalDate.now().plusMonths(1)
    private val DEFAULT_START_DATE = LocalDate.now().plusDays(1)
    private val INVALID_START_DATE: String? = null
    private val INVALID_END_DATE: String? = null

    private val gson = Gson()

    @Test
    fun listAvailability()
    {
        val expected = START_DATE.datesUntil(END_DATE).toList()

        whenever(availabilityServiceMock.listAvailableDates(START_DATE.toString(), END_DATE.toString())).thenReturn(expected)

        val actual = availabilityController.listAvailability(START_DATE.toString(), END_DATE.toString())

        verify(availabilityServiceMock).listAvailableDates(START_DATE.toString(), END_DATE.toString())
        Assert.assertEquals(gson.toJson(expected.joinToString()), actual.body)
    }

    @Test
    fun canHandleNullArrivalDate()
    {
        val expected = DEFAULT_START_DATE.datesUntil(END_DATE).toList()

        whenever(availabilityServiceMock.listAvailableDates(DEFAULT_START_DATE.toString(), END_DATE.toString())).thenReturn(expected)

        val actual = availabilityController.listAvailability(INVALID_START_DATE, END_DATE.toString())

        verify(availabilityServiceMock).listAvailableDates(DEFAULT_START_DATE.toString(), END_DATE.toString())
        Assert.assertEquals(gson.toJson(expected.joinToString()), actual.body)
    }

    @Test
    fun canHandleNullDepartureDate()
    {
        val expected = START_DATE.datesUntil(DEFAULT_END_DATE).toList()

        whenever(availabilityServiceMock.listAvailableDates(START_DATE.toString(), DEFAULT_END_DATE.toString())).thenReturn(expected)

        val actual = availabilityController.listAvailability(START_DATE.toString(), INVALID_END_DATE)

        verify(availabilityServiceMock).listAvailableDates(START_DATE.toString(), DEFAULT_END_DATE.toString())
        Assert.assertEquals(gson.toJson(expected.joinToString()), actual.body)
    }
}