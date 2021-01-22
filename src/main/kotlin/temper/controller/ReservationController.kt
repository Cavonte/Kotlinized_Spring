package temper.controller

import com.google.gson.Gson
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import temper.exception.InvalidInputException
import temper.service.ReservationService

@RestController
class ReservationController(val reservationService: ReservationService)
{
    private val logger = LoggerFactory.getLogger(ReservationController::class.java)
    private val gson = Gson()

    @PostMapping("/reservation", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun bookReservation(@RequestParam(name = "email", required = true) email: String,
                        @RequestParam(name = "fName", required = true) fName: String,
                        @RequestParam(name = "lName", required = true) lName: String,
                        @RequestParam(name = "aDate", required = true) arrivalDate: String,
                        @RequestParam(name = "dDate", required = true) departureDate: String): ResponseEntity<String>
    {
        val bookingIdentifier: String
        try
        {
            bookingIdentifier = reservationService.bookReservation(email, fName, lName, arrivalDate, departureDate)
        } catch (exception: InvalidInputException)
        {
            logger.error("$email, $fName, $lName, $arrivalDate, $departureDate :  ${exception.message}")
            return ResponseEntity.badRequest().body(gson.toJson(exception.message))
        } catch (exception: Exception)
        {
            logger.error("$email, $fName, $lName, $arrivalDate, $departureDate :  ${exception.message}")
            return ResponseEntity.badRequest().body(gson.toJson("Unexpected Error."))
        }

        logger.info("Success:  $bookingIdentifier")
        return ResponseEntity.ok(gson.toJson("BookingId: $bookingIdentifier"))
    }

    @PutMapping("/modifyReservation", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun modifyReservation(@RequestParam(name = "email", required = true) email: String,
                          @RequestParam(name = "aDate", required = true) arrivalDate: String,
                          @RequestParam(name = "dDate", required = true) departureDate: String,
                          @RequestParam(name = "bookingIdentifier", required = true) bookingIdentifier: String): ResponseEntity<String>
    {
        try
        {
            reservationService.modifyReservation(email, arrivalDate, departureDate, bookingIdentifier)
        } catch (exception: InvalidInputException)
        {
            logger.error("$email, $arrivalDate, $departureDate :  ${exception.message}")
            return ResponseEntity.badRequest().body((gson.toJson(exception.message)))
        } catch (exception: Exception)
        {
            logger.error("$email, $arrivalDate, $departureDate :  ${exception.message}")
            return ResponseEntity.badRequest().body(gson.toJson("Unexpected Error."))
        }

        logger.info("Success:  $bookingIdentifier")
        return ResponseEntity.ok((gson.toJson("BookingId: $bookingIdentifier")))
    }

    @DeleteMapping("/cancelReservation")
    fun cancelReservation(@RequestParam(name = "email", required = true) email: String,
                          @RequestParam(name = "bookingIdentifier", required = true) bookingIdentifier: String): ResponseEntity<String>
    {
        try
        {
            reservationService.cancelReservation(email, bookingIdentifier)
        } catch (exception: InvalidInputException)
        {
            logger.error("$email, $bookingIdentifier", exception.message)
            return ResponseEntity.badRequest().body((gson.toJson(exception.message)))
        } catch (exception: Exception)
        {
            logger.error("$email, $bookingIdentifier :  ${exception.message}")
            return ResponseEntity.badRequest().body(gson.toJson("Unexpected Error."))
        }

        logger.info("Success:  $bookingIdentifier")
        return ResponseEntity.ok((gson.toJson("Canceled: $bookingIdentifier")))
    }
}