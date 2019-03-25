package uk.gov.companieshouse.extensions.api.attachments;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * The metadata related to the response returned when a file is in S3 
 * awaiting virus scanning
 *
 */
@Getter
@Setter
@AllArgsConstructor
public class AttachmentsMetadata implements Serializable {
	
	private static final long serialVersionUID = -6411409736141740990L;
	
	private String accessUrl;
	private String scanResult;
}
