package temper.constants

enum class RegexPatterns(val regexPattern: String)
{
    NAME("""[a-zA-Z]* {0,2}[a-zA-Z]+"""),
    BOOKING_IDENTIFIER("""${NAME.regexPattern}_[^\s@_]+@[^\s@]+\.[^\s@_]{2,}_[0-9]{1,7}""")
}