package uk.gov.companieshouse.extensions.api.attachments;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class Attachment {

    private UUID id;

    private String name;

    @JsonProperty("content_type")
    private String contentType;

    private long size;

    // TODO - should these be just 2 class fields - download + self?
    private Map<String, String> links;
}
