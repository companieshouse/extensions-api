package uk.gov.companieshouse.extensions.api.attachments;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Objects;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.service.links.Links;
import uk.gov.companieshouse.service.rest.ApiObjectImpl;

public class AttachmentDTO extends ApiObjectImpl implements Serializable {

    private final long size;
    private final String name;
    private final String contentType;
    private final String id;

    public AttachmentDTO(String id, Links links, String etag, String name, long size, String contentType) {
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
                file.getOriginalFilename(), file.getSize(), file.getContentType());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AttachmentDTO that = (AttachmentDTO) o;
        return size == that.size &&
            Objects.equals(name, that.name) &&
            Objects.equals(contentType, that.contentType) &&
            Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), size, name, contentType, id);
    }
}
