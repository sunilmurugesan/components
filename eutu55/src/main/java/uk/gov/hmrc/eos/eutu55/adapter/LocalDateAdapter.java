package uk.gov.hmrc.eos.eutu55.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    @Override
    public LocalDate unmarshal(String inputDate) {
        return LocalDate.parse(inputDate);
    }

    @Override
    public String marshal(LocalDate inputDate) {
        return inputDate.toString();
    }
}
