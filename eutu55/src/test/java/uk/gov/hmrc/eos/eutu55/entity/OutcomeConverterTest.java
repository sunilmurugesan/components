package uk.gov.hmrc.eos.eutu55.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.REJECTED;

class OutcomeConverterTest {

    private OutcomeConverter converter = new OutcomeConverter();

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Entity outcome enum value can be converted to Database outcome boolean value on Save")
    void entityOutcomeValueCanBeConvertedToCorrectDatabaseValue() {
        assertThat(converter.convertToDatabaseColumn(ACCEPTED), equalTo(TRUE));
        assertThat(converter.convertToDatabaseColumn(REJECTED), equalTo(FALSE));
    }

    @Test
    @DisplayName("Database outcome boolean value can be converted to Entity enum value on Retrieve")
    void databaseOutcomeValueCanBeConvertedToCorrectEntityValue() {
        assertThat(converter.convertToEntityAttribute(TRUE), equalTo(ACCEPTED));
        assertThat(converter.convertToEntityAttribute(FALSE), equalTo(REJECTED));
    }
}