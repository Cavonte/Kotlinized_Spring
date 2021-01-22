package temper.controller

import com.google.gson.Gson
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import temper.constants.ReservationConstraints
import temper.exception.InvalidInputException
import temper.service.AvailabilityService
import java.time.LocalDate


@RestController
class AvailabilityController(val availabilityService: AvailabilityService)
{
    private val logger = LoggerFactory.getLogger(AvailabilityController::class.java)
    private val gson = Gson()

    @GetMapping("/availability", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listAvailability(@RequestParam(name = "startDate", required = true) startDate: String?,
                         @RequestParam(name = "endDate", required = true) endDate: String? = LocalDate.now().plusMonths(1).toString()): ResponseEntity<String>
    {
        var parsedStartDate = startDate
        var parsedEndDate = endDate
        val dateList: List<LocalDate>

        try
        {
            if (startDate == null) parsedStartDate = LocalDate.now().plusDays(1).toString()
            if (endDate == null) parsedEndDate = LocalDate.now().plusDays(ReservationConstraints.MAX_ADVANCE_RESERVATION_DATE.days.toLong()).toString()

            dateList = availabilityService.listAvailableDates(parsedStartDate!!, parsedEndDate!!)
        } catch (exception: InvalidInputException)
        {
            logger.error("$startDate, $endDate :  ${exception.message}")
            return ResponseEntity.badRequest().body((gson.toJson(exception.message)))
        } catch (exception: Exception)
        {
            logger.error("$startDate, $endDate :  ${exception.message}")
            return ResponseEntity.badRequest().body(gson.toJson("Unexpected Error."))
        }

        return ResponseEntity.ok(gson.toJson(dateList.joinToString(", ")))
    }
}