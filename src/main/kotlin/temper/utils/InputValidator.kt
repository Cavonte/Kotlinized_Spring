package temper.utils

import org.apache.commons.validator.routines.EmailValidator
import org.springframework.stereotype.Component
import temper.constants.ErrorMessages
import temper.constants.RegexPatterns
import temper.constants.ReservationConstraints
import temper.exception.InvalidInputException
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Component
class InputValidator(
        private val dateUtil: DateUtil
)
{
    fun validateEmailString(tentativeEmail: String?)
    {
        if (tentativeEmail == null || !isValidEmail(tentativeEmail))
        {
            throw InvalidInputException(ErrorMessages.MALFORMED_EMAIL.message)
        }
    }

    fun validateBookingIdentifier(bookingIdentifier: String?)
    {
        if (bookingIdentifier == null || !isValidBookingIdentifier(bookingIdentifier))
        {
            throw InvalidInputException(ErrorMessages.MALFORMED_BOOKING_IDENTIFIER.message)
        }
    }

    fun validateNameStrings(tentativeFirstName: String?, tentativeLastName: String?)
    {
        if (tentativeFirstName == null || !isValidaName(tentativeFirstName))
        {
            throw InvalidInputException(ErrorMessages.MALFORMED_FIRST_NAME.message)
        }

        if (tentativeLastName == null || !isValidaName(tentativeLastName))
        {
            throw InvalidInputException(ErrorMessages.MALFORMED_LAST_NAME.message)
        }
    }

    fun validateStayDateString(tentativeArrivalDate: String?, tentativeDepartureDate: String?)
    {
        if (tentativeArrivalDate == null || !dateUtil.isValidDateString(tentativeArrivalDate))
        {
            throw InvalidInputException(ErrorMessages.MALFORMED_ARRIVAL_DATE.message)
        }

        if (tentativeDepartureDate == null || !dateUtil.isValidDateString(tentativeDepartureDate))
        {
            throw InvalidInputException(ErrorMessages.MALFORMED_DEPARTURE_DATE.message)
        }
    }

    fun validateDateString(dateString: String?)
    {
        if (dateString == null || !dateUtil.isValidDateString(dateString))
        {
            throw InvalidInputException(ErrorMessages.MALFORMED_DATE_PARAMETER.message)
        }
    }

    fun validateArrivalDate(arrivalDate: LocalDate, departureDate: LocalDate)
    {
        if (dateUtil.isArrivalDateInThePast(arrivalDate))
        {
            throw InvalidInputException(ErrorMessages.DATE_IN_THE_PAST.message)
        }

        if (dateUtil.isArrivalDateBeforeMinimum(arrivalDate))
        {
            throw InvalidInputException(ErrorMessages.DATE_TOO_SOON.message)
        }

        if (arrivalDate == departureDate)
        {
            throw InvalidInputException(ErrorMessages.DEPARTURE_DATE_TOO_SOON.message)
        }

        if (dateUtil.isArrivalDateTooFar(arrivalDate))
        {
            throw InvalidInputException(ErrorMessages.DATE_TOO_FAR.message)
        }

        if (dateUtil.isArrivalDateAfterDepartureDate(arrivalDate, departureDate))
        {
            throw InvalidInputException(ErrorMessages.DEPARTURE_DATE_TOO_SOON.message)
        }

        if (ChronoUnit.DAYS.between(arrivalDate, departureDate) > ReservationConstraints.MAX_DURATION_STAY.days)
        {
            throw InvalidInputException(ErrorMessages.RESERVATION_LIMIT_REACHED.message)
        }
    }

    fun validateAvailabilityDates(arrivalDate: LocalDate, departureDate: LocalDate)
    {
        if (dateUtil.isArrivalDateInThePast(arrivalDate))
        {
            throw InvalidInputException(ErrorMessages.DATE_IN_THE_PAST.message)
        }

        if (dateUtil.isArrivalDateAfterDepartureDate(arrivalDate, departureDate))
        {
            throw InvalidInputException(ErrorMessages.DEPARTURE_DATE_TOO_SOON.message)
        }
    }

    private fun isValidEmail(emailString: String): Boolean
    {
        return EmailValidator.getInstance().isValid(emailString)
    }

    private fun isValidBookingIdentifier(bookingIdentifier: String): Boolean
    {
        return Regex(RegexPatterns.BOOKING_IDENTIFIER.regexPattern).matches(bookingIdentifier)
    }

    private fun isValidaName(nameString: String): Boolean
    {
        return Regex(RegexPatterns.NAME.regexPattern).matches(nameString)
    }
}