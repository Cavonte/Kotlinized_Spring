package temper.constants

enum class ReservationConstraints(val days: Int)
{
    MAX_DURATION_STAY(3),
    MAX_ADVANCE_RESERVATION_DATE(31),
    MIN_RESERVATION_DATE_BUFFER(1),
}