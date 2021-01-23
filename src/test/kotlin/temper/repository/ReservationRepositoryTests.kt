package temper.repository

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import junit.framework.Assert.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import temper.entity.Reservation
import temper.service.AvailabilityService
import temper.service.ReservationService
import temper.utils.DateUtil
import temper.utils.InputValidator
import temper.utils.TestUtils
import java.time.LocalDate
import kotlin.streams.toList

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReservationRepositoryTests
{
    @Autowired
    lateinit var reservationRepository: ReservationRepository

    private val BOOKING_ID = "dedicated_wham@email.com_1234567"
    private val FAKE_BOOKING_ID = "wham@email.com_1234567"
    private val EMAIL = "suburban_daredevil@email.com"
    private val START_DATE = LocalDate.now()
    private val END_DATE = LocalDate.now().plusDays(4)
    private val RESERVATION_LIST = mutableListOf<Reservation>()

    private val availabilityServiceMock: AvailabilityService = mock()
    private val inputValidator = InputValidator(DateUtil())
    private val testUtil: TestUtils = mock()
    lateinit var reservationService: ReservationService

    @BeforeAll
    fun setupReservation()
    {
        START_DATE.datesUntil(END_DATE).forEach { date -> RESERVATION_LIST.add(Reservation(null, date, BOOKING_ID, EMAIL)) }
        reservationRepository.saveAll(RESERVATION_LIST)

        reservationService = ReservationService(availabilityServiceMock, inputValidator, reservationRepository, testUtil)
    }

    @Test
    fun transactionCommitTest()
    {
        whenever(testUtil.getSubtractedDate(any(), any())).thenCallRealMethod()
        reservationService.modifyReservation(EMAIL, START_DATE.plusDays(2).toString(), END_DATE.toString(), BOOKING_ID)
        assertFalse(reservationRepository.existsByReservationDateIn(listOf(LocalDate.now())))
    }

    @Test
    fun transactionRollbackTest()
    {
        whenever(testUtil.getSubtractedDate(any(), any())).thenThrow(RuntimeException("Unchecked Exception"))
        try
        {
            reservationService.modifyReservation(EMAIL, START_DATE.plusDays(2).toString(), END_DATE.toString(), BOOKING_ID)
        } catch (exception: RuntimeException)
        {
        }
        assertTrue(reservationRepository.existsByReservationDateIn(listOf(LocalDate.now())))
    }

    @Test
    fun findByExistingBookingId()
    {
        assertTrue(reservationRepository.existsByBookingIdentifier(BOOKING_ID))
    }

    @Test
    fun reservationDoesNotExist()
    {
        assertFalse(reservationRepository.existsByBookingIdentifier(FAKE_BOOKING_ID))
    }

    @Test
    fun findByExistingDates()
    {
        assertTrue(reservationRepository.existsByReservationDateIn(listOf(LocalDate.now())))
    }

    @Test
    fun dateDoesNotExist()
    {
        assertFalse(reservationRepository.existsByReservationDateIn(listOf(LocalDate.now().minusDays(15))))
    }

    @Test
    fun findByBookingIdentifier()
    {
        val savedList = reservationRepository.findByBookingIdentifier(BOOKING_ID)
        assertEquals(savedList, RESERVATION_LIST)
        assertTrue(savedList.size == RESERVATION_LIST.size)
    }

    @Test
    fun findByBookingIdentifierEmpty()
    {
        val savedList = reservationRepository.findByBookingIdentifier(FAKE_BOOKING_ID)
        assertTrue(savedList.isEmpty())
    }

    @Test
    fun findByLoneDateList()
    {
        val savedList = reservationRepository.findByReservationDateIn(listOf(LocalDate.now()))
        assertEquals(savedList, listOf(RESERVATION_LIST[0]))
        assertTrue(savedList.size == 1)
    }

    @Test
    fun findByDateList()
    {
        val savedList = reservationRepository.findByReservationDateIn(START_DATE.datesUntil(END_DATE).toList())
        assertEquals(savedList, RESERVATION_LIST)
    }

    @Test
    fun noReservationWithDate()
    {
        val savedList = reservationRepository.findByReservationDateIn(listOf(LocalDate.now().minusDays(15)))
        assertTrue(savedList.isEmpty())
    }

    @AfterAll
    fun cleanup()
    {
        reservationRepository.deleteAll()
    }
}