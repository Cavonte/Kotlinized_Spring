package temo

import scala.concurrent.duration._

import scala.util.Random
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class RecordedSimulation extends Simulation {

	val x = Random.alphanumeric.dropWhile(_.isDigit)

	val httpProtocol = http
		.baseUrl("http://localhost:8080")
		.inferHtmlResources(BlackList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*detectportal\.firefox\.com.*"""), WhiteList())
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-US,en;q=0.5")
		.doNotTrackHeader("1")
		.upgradeInsecureRequestsHeader("1")
		.userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:84.0) Gecko/20100101 Firefox/84.0")

	val headers_0 = Map(
		"Cache-Control" -> "max-age=0",
		"Sec-GPC" -> "1")

	val headers_1 = Map(
		"Cache-Control" -> "max-age=0, no-cache",
		"Pragma" -> "no-cache",
		"Sec-GPC" -> "1")

	val scn = scenario("RecordedSimulation")
		.exec(http("request_0")
			.get("/availability")
			.headers(headers_0))
		.pause(1)
		.exec(http("request_1")
			.post("/reservation?email=some"+ x(0) + "eemail@email.com&fName=Bar"+ x(1) + "ry&lName=Allen&aDate=2021-02-01&dDate=2021-02-0"+ Random.nextInt(4))
			.headers(headers_1))
		.pause(1)
		.exec(http("request_2")
			.get("/availability")
			.headers(headers_1))
		.pause(1)
		.exec(http("request_3")
			.get("/availability?startDate=2021-02-1"+ Random.nextInt(1) +"&endDate=2021-02-1"+ Random.nextInt(4))
			.headers(headers_1))
		.pause(2)
		.exec(http("request_4")
			.get("/availability?startDate=2021-02-01&endDate=2021-02-02")
			.headers(headers_1))
		.pause(1)
		.exec(http("request_5")
			.get("/availability")
			.headers(headers_1))
		.pause(2)
		.exec(http("request_6")
			.post("/reservation?email=someeemail@email.com&fName=Barry&lName=Allen&aDate=2021-01-23&dDate=2021-01-24")
			.headers(headers_1))
		.pause(1)
		.exec(http("request_7")
			.post("/reservation?email=foo"+ x(0) + "eemail@email.com&fName=Barry&lName=Allen&aDate=2021-02-10&dDate=2021-02-1"+ Random.nextInt(4))
			.headers(headers_1))
		.pause(2)
		.exec(http("request_8")
			.get("/availability")
			.headers(headers_1))
		.pause(2)
		.exec(http("request_9")
			.get("/availability?startDate=2021-02-0"+ Random.nextInt(1) +"&endDate=2021-02-0"+ Random.nextInt(4))
			.headers(headers_1))
		.pause(1)
		.exec(http("request_10")
			.get("/availability")
			.headers(headers_1))
		.pause(2)
		.exec(http("request_11")
			.put("/modifyReservation?email=som"+ x(3) + "eemail@email.com&aDate=2021-01-23&dDate=2021-01-26&bookingIdentifier=Allen_someeemail@email.com_6313974")
			.headers(headers_1))
		.pause(2)
		.exec(http("request_12")
			.get("/availability")
			.headers(headers_1))
		.pause(2)
		.exec(http("request_13")
			.get("/availability?startDate=2021-02-11&endDate=2021-02-02")
			.headers(headers_1))
		.pause(2)
		.exec(http("request_14")
			.get("/availability")
			.headers(headers_1))
		.pause(2)
		.exec(http("request_15")
			.get("/availability?startDate=2021-02-11&endDate=2021-02-22")
			.headers(headers_1))
		.pause(2)
		.exec(http("request_16")
			.get("/availability")
			.headers(headers_1))
		.pause(2)
		.exec(http("request_17")
			.post("/reservation?email=some"+ x(6) + "eemail@email.com&fName=Bar"+ x(3) + "ry&lName=Allen&aDate=2021-02-01&dDate=2021-02-0"+ Random.nextInt(4))
			.headers(headers_1))
		.pause(2)
		.exec(http("request_18")
			.get("/availability?startDate=2021-01-30&endDate=2021-02-02")
			.headers(headers_1))
		.pause(2)
		.exec(http("request_19")
			.get("/availability?startDate=2021-01-30&endDate=2021-02-02")
			.headers(headers_1))
		.pause(2)
		.exec(http("request_20")
			.get("/availability?startDate=2021-01-01&endDate=2021-02=02")
			.headers(headers_1))
		.pause(2)
		.exec(http("request_21")
			.get("/availability?startDate=2021-02-11&endDate=2021-02-22")
			.headers(headers_1))

	setUp(
  		scn.inject(
    	rampUsersPerSec(10).to(300).during(15.seconds).randomized
  		).protocols(httpProtocol)
	)
}