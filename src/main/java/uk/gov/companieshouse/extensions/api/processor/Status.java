package uk.gov.companieshouse.extensions.api.processor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Status {
    StatusEnum status = StatusEnum.OPEN;
}
