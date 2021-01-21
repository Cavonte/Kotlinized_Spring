package temper.repository

import junit.framework.Assert.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import temper.entity.Reservation
import java.time.LocalDate
import kotlin.streams.toList

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReservationRepositoryTests
{
    @Autowired
    lateinit var reservationRepository: ReservationRepository

    private val NAME = "name1"
    private val BOOKING_ID = "a_booking_id"
    private val FAKE_BOOKING_ID = "a_fake_booking_id"
    private val EMAIL = "suburban_daredevil"
    val startDate = LocalDate.now()
    val endDate = LocalDate.now().plusDays(10)
    val reservationList = mutableListOf<Reservation>()

    @BeforeAll
    fun setupReservation()
    {
        startDate.datesUntil(endDate).forEach { date -> reservationList.add(Reservation(null, date, BOOKING_ID, EMAIL)) }
        reservationRepository.saveAll(reservationList)
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
        assertEquals(savedList, reservationList)
        assertTrue(savedList.size == reservationList.size)
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
        assertEquals(savedList, listOf(reservationList[0]))
        assertTrue(savedList.size == 1)
    }

    @Test
    fun findByDateList()
    {
        val savedList = reservationRepository.findByReservationDateIn(startDate.datesUntil(endDate).toList())
        assertEquals(savedList, reservationList)
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