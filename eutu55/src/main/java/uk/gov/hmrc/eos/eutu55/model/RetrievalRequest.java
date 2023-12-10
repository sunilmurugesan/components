package uk.gov.hmrc.eos.eutu55.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RetrievalRequest {
    @PastOrPresent
    @NotNull
    private LocalDate fromDate;

    public uk.gov.hmrc.eu.eutu55.retrieval.RetrievalRequest transform() {
        uk.gov.hmrc.eu.eutu55.retrieval.RetrievalRequest retrievalRequest = new uk.gov.hmrc.eu.eutu55.retrieval.RetrievalRequest();
        try{
            retrievalRequest.setFromDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(fromDate.toString()));
        }catch (DatatypeConfigurationException ex){
            throw new IllegalArgumentException();
        }

        return retrievalRequest;
    }
}
