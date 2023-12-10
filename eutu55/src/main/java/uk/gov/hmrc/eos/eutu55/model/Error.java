package uk.gov.hmrc.eos.eutu55.model;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class Error {
    String code;
    String description;
}