package uk.gov.hmrc.eos.eutu55.utils;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import uk.gov.hmrc.eos.eutu55.exception.EUTU55Exception;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;


class JsonSerialiserTest {

    @Test
    void toJsonTextTest() {

        String jsonText = JsonSerialiser.serialise(new Object() {private String field1 = "this is field 1";private String field2 = "this is field 2";public String getField1() {return field1;}public String getField2() {return field2;}});
        MatcherAssert.assertThat(StringUtils.deleteWhitespace(jsonText), equalTo(StringUtils.deleteWhitespace("{\"field1\":\"this is field 1\",\"field2\":\"this is field 2\"}")));

    }

    @Test
    void toJsonTextThrowExceptionTest() {
        /*
        Note: By default the underlying Jackson ObjectMapper class that JsonSerialiser uses is unable to cope with an object which doesn't have
        any fields or accessor methods. Therefore this can be used to check JsonSerialiser will raise a EUTU55Exception when necessary.
         */
        assertThrows(EUTU55Exception.class, () -> JsonSerialiser.serialise(new Object()));

    }

}
