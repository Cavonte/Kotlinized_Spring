package temper.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import temper.entity.Reservation
import java.time.LocalDate

@Repository
interface ReservationRepository : JpaRepository<Reservation, Long>
{
    fun deleteByReservationDateIn(reservationDates: Iterable<LocalDate>): Int

    fun existsByReservationDateIn(reservationDates: List<LocalDate>): Boolean

    fun existsByBookingIdentifier(bookingIdentifier: String): Boolean

    fun findByBookingIdentifier(bookingIdentifier: String): List<Reservation>

    fun findByReservationDateIn(dates: List<LocalDate>): List<Reservation>
}
