package uk.gov.companieshouse.extensions.api.attachments;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.service.rest.ApiObjectImpl;

import java.io.Serializable;

public class Attachment extends ApiObjectImpl implements Serializable {

    private String id;

    private String name;

    @JsonProperty("content_type")
    private String contentType;

    private long size;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
}
