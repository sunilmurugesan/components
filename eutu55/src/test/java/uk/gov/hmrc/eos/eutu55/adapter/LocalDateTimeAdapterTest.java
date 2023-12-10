package uk.gov.hmrc.eos.eutu55.adapter;

import org.exparity.hamcrest.date.LocalDateTimeMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class LocalDateTimeAdapterTest {

    private LocalDateTimeAdapter adapter = new LocalDateTimeAdapter();

    @Test
    @DisplayName("Xml datetime can be transformed to java Local datetime on unmarshalling")
    void xmlDateCanBeTransformedToLocalDate() {
        LocalDateTime localDateTime = adapter.unmarshal("2021-05-04T18:13:51.0");
        assertThat(localDateTime, LocalDateTimeMatchers.sameDay(LocalDateTime.of(2021, 5, 4, 18, 13, 51, 0)));
    }


    @Test
    @DisplayName("Java datetime can be transformed to xml datetime on marshalling")
    void javaDateCanBeTransformedToXmlDate() {
        String xmlDate = adapter.marshal(LocalDateTime.of(2021, 5, 4, 18, 13, 51, 0));
        assertThat(xmlDate, equalTo("2021-05-04T18:13:51.0"));
    }

}