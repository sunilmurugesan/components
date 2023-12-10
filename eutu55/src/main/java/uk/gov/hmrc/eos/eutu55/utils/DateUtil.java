package uk.gov.hmrc.eos.eutu55.utils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class DateUtil {

    /**
     * This method converts given LocalDate to SQL Date
     *
     * @param localDate {@link LocalDate}
     * @return {@link Date}
     */
    public static Date toSqlDate(LocalDate localDate) {
        return Optional.ofNullable(localDate)
                .map(Date::valueOf)
                .orElse(null);
    }

    /**
     * This method converts given SQL Date to LocalDate
     *
     * @param date {@link Date}
     * @return {@link LocalDate}
     */
    public static LocalDate toLocalDate(Date date) {
        return Optional.ofNullable(date)
                .map(Date::toLocalDate)
                .orElse(null);
    }

    /**
     * This method converts given LocalDateTime to Timestamp
     *
     * @param localDateTime {@link LocalDateTime}
     * @return {@link Timestamp}
     */
    public static Timestamp toSqlTimestamp(LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime)
                .map(Timestamp::valueOf)
                .orElse(null);
    }

    /**
     * This method converts given Timestamp to LocalDateTime
     *
     * @param timestamp {@link Timestamp}
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return Optional.ofNullable(timestamp)
                .map(Timestamp::toLocalDateTime)
                .orElse(null);
    }

    /**
     * This method is used to convert XMLGregorianCalendar value to java.time.LocalDate.
     *
     * If the parameter xmlDate is null, null is returned
     *
     * @param xmlDate - The source date
     * @return localDate {@link LocalDate}
     */
    public static LocalDate localDateFromXmlDate(final XMLGregorianCalendar xmlDate) {
        if (xmlDate != null) {
            return xmlDate.toGregorianCalendar().toZonedDateTime().toLocalDate();
        }
        return null;
    }

    /**
     * This method is used to convert XMLGregorianCalendar value to java.time.LocalDateTime.
     *
     * If the parameter xmlDate is null, null is returned
     *
     * @param xmlDate - The source date
     * @return localDateTime {@link LocalDateTime}
     */
    public static LocalDateTime localDateTimeFromXmlDate(final XMLGregorianCalendar xmlDate) {
        if (xmlDate != null) {
            return xmlDate.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
        }
        return null;
    }
}
