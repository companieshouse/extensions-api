package uk.gov.companieshouse.extensions.api.attachments;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.service.links.Links;
import uk.gov.companieshouse.service.rest.ApiObjectImpl;

import java.io.Serializable;

public class AttachmentDTO extends ApiObjectImpl implements Serializable {

    private long size;
    private String name;
    private String contentType;
    private String id;

    public AttachmentDTO(String id, Links links, String etag, String name, long size,
                         String contentType) {
        this.id = id;
        setLinks(links);
        setEtag(etag);
        this.name = name;
        this.size = size;
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public String getContentType() {
        return contentType;
    }

    public String getId() {
        return id;
    }

    @JsonIgnore
    public static Builder builder() {
        return new Builder();
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
    public static class Builder {
        private Attachment attachment;
        private MultipartFile file;
        private Links links;

        public Builder withAttachment(Attachment attachment) {
            this.attachment = attachment;
            return this;
        }

        public Builder withFile(MultipartFile file) {
            this.file = file;
            return this;
        }

        public Builder withLinks(Links links) {
            this.links = links;
            return this;
        }

        public AttachmentDTO build() {
            return new AttachmentDTO(attachment.getId(), links, "",
                file.getName(), file.getSize(), file.getContentType());
        }
    }
}
