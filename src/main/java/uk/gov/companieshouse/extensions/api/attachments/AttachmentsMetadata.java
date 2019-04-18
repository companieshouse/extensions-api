package uk.gov.companieshouse.extensions.api.attachments;

import uk.gov.companieshouse.service.rest.ApiObjectImpl;

import java.io.Serializable;

/**
 * The metadata related to the response returned when a file is in S3 
 * awaiting virus scanning
 *
 */
public class AttachmentsMetadata extends ApiObjectImpl implements Serializable {

    private static final long serialVersionUID = -6411409736141740990L;

    private String id;
    private long size;
    private String accessUrl;
    private String scanResult;

    public AttachmentsMetadata(String accessUrl, String scanResult) {
        this.accessUrl = accessUrl;
        this.scanResult = scanResult;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    public String getScanResult() {
        return scanResult;
    }

    public void setScanResult(String scanResult) {
        this.scanResult = scanResult;
    }
}
