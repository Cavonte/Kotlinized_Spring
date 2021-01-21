package temper.constants

enum class ErrorMessages(val message: String)
{
    MALFORMED_DEPARTURE_DATE("The departure date does not match expected format. e.g. (2001-01-01)"),
    MALFORMED_ARRIVAL_DATE("The arrival date does not match expected format. e.g. (2001-01-01)"),
    MALFORMED_DATE_PARAMETER("The date parameter does not match the expected format. e.g. (2001-01-01)"),
    MALFORMED_EMAIL("Invalid Email Provided."),
    MALFORMED_FIRST_NAME("Invalid First Name provided."),
    MALFORMED_LAST_NAME("Invalid Last Name provided."),
    MALFORMED_BOOKING_IDENTIFIER("Invalid booking identifier provided."),

    RESERVATION_DOES_NOT_EXIST("Reservation does not exist."),

    UNAVAILABLE_RESERVATION_DATES("One of the selected date(s) is not available."),
    RESERVATION_LIMIT_REACHED("Reservation cap reached. Max allowed duration is ${ReservationConstraints.MAX_DURATION_STAY.days} day(s)."),
    DATE_IN_THE_PAST("Reservation start date must be in the future."),
    DATE_TOO_FAR("Reservation arrival date must be within ${ReservationConstraints.MAX_ADVANCE_RESERVATION_DATE.days} day(s)."),
    DATE_TOO_SOON("Reservation must be at least ${ReservationConstraints.MIN_RESERVATION_DATE_BUFFER.days} day(s) in the future."),
    DEPARTURE_DATE_TOO_SOON("The departure date must be after the arrival date.")
}