package uk.gov.companieshouse.extensions.api.processor;

public enum StatusEnum {
    OPEN("Open");

    private String description;

    private StatusEnum(String desc) {
      this.description = desc;
    }
}
