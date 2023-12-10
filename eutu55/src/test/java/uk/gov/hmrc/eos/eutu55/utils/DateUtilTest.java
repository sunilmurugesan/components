package uk.gov.hmrc.eos.eutu55.utils;

import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uk.gov.hmrc.eos.eutu55.utils.DateUtil.*;

class DateUtilTest {

    @Test
    void convertToSqlDateByPassingNullReturnsNull() {
        Date date = toSqlDate(null);
        assertThat(date, nullValue());
    }

    @Test
    void convertToSqlDateByPassingLocalDateReturnsSqlDate() {
        LocalDate localDate = LocalDate.now();
        Date date = toSqlDate(localDate);
        assertThat(date, notNullValue());
        assertThat(date.toLocalDate(), equalTo(localDate));
    }


    @Test
    void toLocalDateByNullShouldReturnNull() {
        LocalDate date = toLocalDate(null);
        assertThat(date, nullValue());
    }

    @Test
    void toLocalDateBySqlDateShouldReturnLocalDate() {
        Date date = Date.valueOf(LocalDate.now());
        LocalDate localDate = toLocalDate(date);
        assertThat(localDate, equalTo(date.toLocalDate()));
    }

    @Test
    void toTimestampByNullShouldReturnNull() {
        Timestamp timestamp = toSqlTimestamp(null);
        assertThat(timestamp, nullValue());
    }

    @Test
    public void toTimestampByLocalDateTimeShouldReturnTimestamp() {
        LocalDateTime localDateTime = LocalDateTime.now();
        Timestamp timestamp = toSqlTimestamp(localDateTime);
        assertThat(timestamp.toLocalDateTime(), equalTo(localDateTime));
    }

    @Test
    public void toLocalDateTimeByNullShouldReturnNull() {
        LocalDateTime localDateTime = toLocalDateTime(null);
        assertThat(localDateTime, nullValue());
    }

    @Test
    public void toLocalDateTimeByTimestampShouldReturnLocalDateTime() {
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        LocalDateTime localDateTime = toLocalDateTime(timestamp);
        assertThat(localDateTime, equalTo(timestamp.toLocalDateTime()));
    }


}
