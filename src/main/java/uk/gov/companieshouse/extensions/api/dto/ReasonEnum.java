package uk.gov.companieshouse.extensions.api.dto;

import lombok.Getter;

@Getter
public enum ReasonEnum {
	ILLNESS("Illness"), OTHER("Other");
	
	private String description;

    private ReasonEnum(String desc) {
        this.description = desc;
    }
}

