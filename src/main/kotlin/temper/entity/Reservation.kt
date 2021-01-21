package temper.entity

import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "reservation")
data class Reservation(@Id @GeneratedValue val reservationId: Long? = null,
                       val reservationDate: LocalDate = LocalDate.now(),
                       val bookingIdentifier: String = "",
                       val email: String = "")
{
    override fun toString(): String
    {
        return "$reservationDate, $bookingIdentifier"
    }
}
