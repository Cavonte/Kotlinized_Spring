package temper.utils

import org.springframework.stereotype.Component
import temper.constants.ReservationConstraints
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Component
class DateUtil
{
    fun isValidDateString(dateString: String): Boolean
    {
        try
        {
            LocalDate.parse(dateString)
        } catch (exception: DateTimeParseException)
        {
            return false
        }
        return true
    }

    fun isArrivalDateInThePast(arrivalDate: LocalDate): Boolean
    {
        return arrivalDate.isBefore(LocalDate.now())
    }

    fun isArrivalDateBeforeMinimum(arrivalDate: LocalDate): Boolean
    {
        return arrivalDate.isBefore(LocalDate.now().plusDays(ReservationConstraints.MIN_RESERVATION_DATE_BUFFER.days.toLong()))
    }

    fun isArrivalDateTooFar(arrivalDate: LocalDate): Boolean
    {
        return arrivalDate.isAfter(LocalDate.now().plusDays(ReservationConstraints.MAX_ADVANCE_RESERVATION_DATE.days.toLong()))
    }

    fun isArrivalDateAfterDepartureDate(arrivalDate: LocalDate, departureDate: LocalDate): Boolean
    {
        return arrivalDate.isAfter(departureDate)
    }
}