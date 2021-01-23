
# Reservation System

This project is a tentative reservation system for the new island in the Pacific Ocean. Created using Kotlin and Spring.
## Stack
- Kotlin
- Spring Boot/Maven
- Spring Data/JPA
- Flyway
- Mysql

## Endpoints
There are 4 Endpoints available.
- /listAvailable (startDate, endDate)
- /reservation (email, fName, lName, arrivalDate, departureDate) 
- /modifyReservation (email, arrivalDate, departureDate, bookingIdentifier)
- /cancelReservation (email, bookingIdentifier)

#### Example calls
(https://github.com/Cavonte/Kotlinized_spring/blob/main/gatlin/RecordedSimulation.scala) contains examples of calls that can be made to the endpoint. e.g.
```
.exec(http("request_12")
.put("/modifyReservation?email=barry_allen@aol.com&aDate=2021-01-23&dDate=2021-01-26&bookingIdentifier=barry_allen@aol.com_6313975")
.headers(headers_1)
.check(status.is(400)))
```

## Transactions and Load Handling
- This application handles the atomicity of Databases with the @Transaction annotation provided by Spring.
This is tested with a call to the main service class and triggering a runtime exception.
As expected all operations were rollback.
- For the load test. A Gatlin simulation with 300 users gave the following results.
![Simulation Report](https://github.com/Cavonte/Kotlinized_spring/blob/main/300users.PNG)
The entire report can be found here.
(https://github.com/Cavonte/Kotlinized_spring/blob/main/gatlin/recordedsimulation-20210123002812478.7z)

## Installation
Requirements
- Java 11
- Maven
- Mysql Instance (See application.properties for credentials)
- kotlin IDE

### Setup
The application will need a mysql instance.
Use these parameters or update the application.properties file as needed.
```
   spring.flyway.url=jdbc:mysql://localhost:3306
   spring.flyway.user=root
   spring.flyway.password=
```
#### Through the IDE
- Import the pom file/project in your IDE of choice and run
```
mvn clean install
```

- Run the application. The first run perform the migrations, creating the schema, and the tables required.

#### Alternative
- Run a clean install from the root folder and locate the generated jar. (/target/)
```
mvn clean install && mvn package
```
- Run the jar located in the root folder.
```
java -jar reservation.jar
```
