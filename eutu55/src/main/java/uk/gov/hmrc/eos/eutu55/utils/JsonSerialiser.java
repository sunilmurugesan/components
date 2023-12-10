package uk.gov.hmrc.eos.eutu55.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmrc.eos.eutu55.exception.EUTU55Exception;

import static uk.gov.hmrc.eos.eutu55.utils.ErrorCode.EUTU55_JSON_PROCESSING_EXCEPTION;

public class JsonSerialiser {

    private static ObjectMapper mapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    /**
     * Returns a JSON string representation of a java object
     */
    public static String serialise(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException jpe) {
            throw new EUTU55Exception(EUTU55_JSON_PROCESSING_EXCEPTION);
        }
    }
}
