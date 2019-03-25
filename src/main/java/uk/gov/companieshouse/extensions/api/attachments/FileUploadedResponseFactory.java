package uk.gov.companieshouse.extensions.api.attachments;

import org.springframework.http.ResponseEntity;

import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.ServiceResultStatus;
import uk.gov.companieshouse.service.rest.response.ChResponseBody;
import uk.gov.companieshouse.service.rest.response.ResponseEntityFactory;

public class FileUploadedResponseFactory implements ResponseEntityFactory {
	
    @Override
    public <T> ResponseEntity<ChResponseBody<T>> createResponseEntity(
    		ServiceResult<T> serviceResult) {
        ChResponseBody<T> body = ChResponseBody.createNormalBody(serviceResult.getData());
        return ResponseEntity.accepted().body(body);
    }

    @Override
    public ServiceResultStatus getStatusToMatch() {
        return ServiceResultStatus.ACCEPTED;
    }

}
