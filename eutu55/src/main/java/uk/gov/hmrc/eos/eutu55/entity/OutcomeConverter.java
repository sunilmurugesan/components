package uk.gov.hmrc.eos.eutu55.entity;

import uk.gov.hmrc.eos.eutu55.utils.Outcome;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.REJECTED;

@Converter
public class OutcomeConverter implements AttributeConverter<Outcome, Boolean> {

    @Override
    public Boolean convertToDatabaseColumn(Outcome outcome) {
        return outcome.isAccepted();
    }

    @Override
    public Outcome convertToEntityAttribute(Boolean outcomeFlag) {
        return outcomeFlag ? ACCEPTED : REJECTED;
    }
}