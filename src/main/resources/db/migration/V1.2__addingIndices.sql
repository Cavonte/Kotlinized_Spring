ALTER TABLE `temper`.`reservation`
ADD INDEX `id, reservationDate` (`reservationId` ASC, `reservationDate` ASC) INVISIBLE,
ADD INDEX `id, bookingIdentifier` (`reservationId` ASC, `bookingIdentifier` ASC) INVISIBLE,
ADD INDEX `bookingIdentifier` (`bookingIdentifier` ASC) INVISIBLE,
ADD INDEX `reservationDate` (`reservationDate` ASC) VISIBLE;

ALTER TABLE `temper`.`reservation`
ADD UNIQUE INDEX `reservationDate_UNIQUE` (`reservationDate` ASC) VISIBLE;
