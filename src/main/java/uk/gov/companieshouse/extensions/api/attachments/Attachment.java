package uk.gov.companieshouse.extensions.api.attachments;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import uk.gov.companieshouse.service.links.Links;
import java.util.UUID;

@Getter
@Setter
public class Attachment {

    private UUID id;

    private String name;

    @JsonProperty("content_type")
    private String contentType;

    private long size;

    private Links links;
}
