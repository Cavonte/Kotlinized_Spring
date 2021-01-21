ALTER TABLE `temper`.`reservation`
ADD COLUMN `reservationDate` DATE NOT NULL AFTER `reservationId`,
ADD COLUMN `bookingIdentifier` VARCHAR(45) NOT NULL AFTER `reservationDate`,
ADD COLUMN `email` VARCHAR(45) NOT NULL AFTER `bookingIdentifier`;
