package temper.service

import org.springframework.stereotype.Service
import temper.constants.BookingConstants
import temper.constants.ErrorMessages
import temper.entity.Reservation
import temper.entity.User
import temper.exception.InvalidInputException
import temper.repository.ReservationRepository
import temper.utils.InputValidator
import temper.utils.TestUtils
import java.time.LocalDate
import java.util.concurrent.ThreadLocalRandom
import javax.transaction.Transactional
import kotlin.streams.toList

@Service
@Transactional
class ReservationService(private val availabilityService: AvailabilityService,
                         private val inputValidator: InputValidator,
                         private val reservationRepository: ReservationRepository,
                         private val testUtils: TestUtils)
{

    @Throws(InvalidInputException::class)
    fun bookReservation(
            email: String?,
            firstName: String?,
            lastName: String?,
            arrivalDate: String?,
            departureDate: String?
    ): String
    {
        inputValidator.validateStayDateString(arrivalDate, departureDate)
        inputValidator.validateNameStrings(firstName, lastName)
        inputValidator.validateEmailString(email)

        val parsedDepartureDate: LocalDate = LocalDate.parse(departureDate)
        val parsedArrivalDate: LocalDate = LocalDate.parse(arrivalDate)
        inputValidator.validateArrivalDate(parsedArrivalDate, parsedDepartureDate)

        if (!availabilityService.isDateRangeAvailable(arrivalDate!!, departureDate!!))
        {
            throw InvalidInputException(ErrorMessages.UNAVAILABLE_RESERVATION_DATES.message)
        }

        val currentUser = User(null, firstName!!, lastName!!, email!!)
        val bookingIdentifier = generateBookingIdentifier(currentUser)
        val reservations: MutableList<Reservation> = buildReservationList(parsedArrivalDate.datesUntil(parsedDepartureDate).toList(), bookingIdentifier, email)

        saveReservations(reservations)
        return bookingIdentifier
    }

    @Throws(InvalidInputException::class)
    fun modifyReservation(email: String?,
                          arrivalDate: String?,
                          departureDate: String?,
                          bookingIdentifier: String?)
    {
        inputValidator.validateEmailString(email)
        inputValidator.validateBookingIdentifier(bookingIdentifier)

        inputValidator.validateStayDateString(arrivalDate, departureDate)
        val parsedArrivalDate: LocalDate = LocalDate.parse(arrivalDate)
        val parsedDepartureDate: LocalDate = LocalDate.parse(departureDate)

        inputValidator.validateArrivalDate(parsedArrivalDate, parsedDepartureDate)

        isValidReservation(bookingIdentifier!!)

        val requestedDates = parsedArrivalDate.datesUntil(parsedDepartureDate).toList()
        val existingReservations = reservationRepository.findByBookingIdentifier(bookingIdentifier)

        val existingReservationsDates = existingReservations.map { reservation -> reservation.reservationDate }
        val newReservationDates = requestedDates.filter { date -> !existingReservationsDates.contains(date) }

        if (reservationRepository.existsByReservationDateIn(newReservationDates))
        {
            throw InvalidInputException(ErrorMessages.UNAVAILABLE_RESERVATION_DATES.message)
        }

        val newReservations = buildReservationList(newReservationDates, bookingIdentifier, email!!)
        saveReservations(newReservations)

        val outdatedReservationDates = testUtils.getSubtractedDate(existingReservationsDates, requestedDates)
        reservationRepository.deleteByReservationDateIn(outdatedReservationDates)
    }

    @Throws(InvalidInputException::class)
    fun cancelReservation(email: String?,
                          bookingIdentifier: String?)
    {
        inputValidator.validateEmailString(email)
        inputValidator.validateBookingIdentifier(bookingIdentifier)
        isValidReservation(bookingIdentifier!!)

        val resList = reservationRepository.findByBookingIdentifier(bookingIdentifier)

        resList.stream().forEach { reservation ->
            if (!reservation.email.contentEquals(email!!))
            {
                throw InvalidInputException(ErrorMessages.RESERVATION_DOES_NOT_EXIST.message)
            }
        }

        reservationRepository.deleteAll(resList)
    }

    private fun generateBookingIdentifier(user: User): String
    {
        val suffix: Int = ThreadLocalRandom.current().nextInt(0, BookingConstants.BOOKING_ID_SUFFIX.value)
        return "${user.getIdentifier()}_$suffix"
    }

    private fun buildReservationList(dates: List<LocalDate>, bookingIdentifier: String, email: String): MutableList<Reservation>
    {
        val reservations: MutableList<Reservation> = mutableListOf()

        dates.forEach { date ->
            reservations.add(Reservation(null, date, bookingIdentifier, email))
        }

        return reservations
    }

    private fun saveReservations(reservations: MutableList<Reservation>) = reservationRepository.saveAll(reservations)

    private fun isValidReservation(bookingIdentifier: String)
    {
        if (!reservationRepository.existsByBookingIdentifier(bookingIdentifier))
        {
            throw InvalidInputException(ErrorMessages.RESERVATION_DOES_NOT_EXIST.message)
        }
    }
}