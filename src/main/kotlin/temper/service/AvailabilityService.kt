package temper.service

import org.springframework.stereotype.Service
import temper.exception.InvalidInputException
import temper.repository.ReservationRepository
import temper.utils.InputValidator
import java.time.LocalDate
import javax.transaction.Transactional
import kotlin.streams.toList

@Service
@Transactional
class AvailabilityService(private val reservationRepository: ReservationRepository, private val inputValidator: InputValidator)
{
    @Throws(InvalidInputException::class)
    fun listAvailableDates(tentativeStartDate: String?, tentativeEndDate: String?): List<LocalDate>
    {
        inputValidator.validateDateString(tentativeStartDate)
        inputValidator.validateDateString(tentativeEndDate)

        val startDate = LocalDate.parse(tentativeStartDate)
        val endDate = LocalDate.parse(tentativeEndDate)



        inputValidator.validateAvailabilityDates(startDate, endDate)

        val takenDates: List<LocalDate> = getTakenDates(startDate, endDate)

        return getFreeDates(startDate, endDate, takenDates).toList()
    }

    @Throws(InvalidInputException::class)
    fun isDateRangeAvailable(tentativeStartDate: String?, tentativeEndDate: String?): Boolean
    {
        inputValidator.validateDateString(tentativeStartDate)
        inputValidator.validateDateString(tentativeEndDate)

        val startDate = LocalDate.parse(tentativeStartDate)
        val endDate = LocalDate.parse(tentativeEndDate)

        inputValidator.validateArrivalDate(startDate, endDate)

        return getTakenDates(startDate, endDate).isEmpty()
    }

    private fun getFreeDates(startDate: LocalDate, endDate: LocalDate?, takenDates: List<LocalDate>) = startDate.datesUntil(endDate).filter { currentDate -> !takenDates.contains(currentDate) }

    private fun getTakenDates(startDate: LocalDate, endDate: LocalDate) =
            reservationRepository.findByReservationDateIn(getLocalDateList(startDate, endDate)).map { reservation -> reservation.reservationDate }

    private fun getLocalDateList(startDate: LocalDate, endDate: LocalDate): MutableList<LocalDate>
    {
        val dateList = mutableListOf<LocalDate>()

        startDate.datesUntil(endDate).forEach { someDate -> dateList.add(someDate) }

        return dateList
    }
}