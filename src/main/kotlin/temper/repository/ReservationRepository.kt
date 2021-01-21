package temper.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import temper.entity.Reservation
import java.time.LocalDate
import javax.transaction.Transactional

@Repository
interface ReservationRepository : JpaRepository<Reservation, Long>
{
    @Transactional
    fun deleteByReservationDateIn(reservationDates: Iterable<LocalDate>): Int

    @Transactional
    fun existsByReservationDateIn(reservationDates: List<LocalDate>): Boolean

    @Transactional
    fun existsByBookingIdentifier(bookingIdentifier: String): Boolean

    @Transactional
    fun findByBookingIdentifier(bookingIdentifier: String): List<Reservation>

    @Transactional
    fun findByReservationDateIn(dates: List<LocalDate>): List<Reservation>
}
