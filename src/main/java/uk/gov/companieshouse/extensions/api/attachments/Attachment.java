package uk.gov.companieshouse.extensions.api.attachments;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.service.links.Links;
import java.util.UUID;

public class Attachment {

    private String etag;

    private UUID id;

    private String name;

    @JsonProperty("content_type")
    private String contentType;

    private long size;

    private Links links;

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }
}
