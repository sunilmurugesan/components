package uk.gov.hmrc.eos.eutu55.adapter;

import org.exparity.hamcrest.date.LocalDateMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalDateAdapterTest {

    private LocalDateAdapter adapter = new LocalDateAdapter();

    @Test
    @DisplayName("Xml date can be transformed to java Local Date on unmarshalling")
    void xmlDateCanBeTransformedToLocalDate() {
        LocalDate localDate = adapter.unmarshal("2021-05-04");
        assertThat(localDate, LocalDateMatchers.sameDay(LocalDate.of(2021, 5, 4)));
    }

    @Test
    @DisplayName("Invalid xml date cannot be transformed to java Local Date on unmarshalling")
    void invalidXmlDateCanBeTransformedToLocalDate() {
        assertThrows(DateTimeException.class, () -> adapter.unmarshal("2021-13-32"));
    }

    @Test
    @DisplayName("Java date can be transformed to xml date on marshalling")
    void javaDateCanBeTransformedToXmlDate() {
        String xmlDate = adapter.marshal(LocalDate.of(2021, 5, 4));
        assertThat(xmlDate, equalTo("2021-05-04"));
    }

}