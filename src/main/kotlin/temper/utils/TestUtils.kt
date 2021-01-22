package temper.utils

import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class TestUtils
{
    fun getSubtractedDate(set1: List<LocalDate>, set2: List<LocalDate>): Set<LocalDate>
    {
        return set1.subtract(set2)
    }
}