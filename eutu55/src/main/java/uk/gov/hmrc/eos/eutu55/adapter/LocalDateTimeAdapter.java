package uk.gov.hmrc.eos.eutu55.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    @Override
    public LocalDateTime unmarshal(String inputDateTime) {
        return LocalDateTime.parse(inputDateTime);
    }

    @Override
    public String marshal(LocalDateTime inputDateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.n");
        return inputDateTime.format(dateTimeFormatter);
    }
}
