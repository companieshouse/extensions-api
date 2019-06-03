package uk.gov.companieshouse.extensions.api.attachments.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.logger.LogMethodCall;

@Component
public class FileUploader {

    private static final String UPLOAD = "upload";
    private static final String CONTENT_DISPOSITION_VALUE = "form-data; name=%s; filename=%s";
    private static final String HEADER_API_KEY = "x-api-key";

    @Autowired
    private ApiLogger logger;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${FILE_TRANSFER_API_URL}")
    private String fileTransferApiURL;

    @Value("${FILE_TRANSFER_API_KEY}")
    private String fileTransferApiKey;

    @LogMethodCall
    public FileUploaderResponse upload(MultipartFile fileToUpload) {

        FileUploaderResponse fileUploaderResponse;

        HttpHeaders headers = createHttpHeaders();

        LinkedMultiValueMap<String, String> fileHeaderMap = createFileHeader(fileToUpload);

        try {
            HttpEntity<byte[]> fileHttpEntity = new HttpEntity<>(fileToUpload.getBytes(), fileHeaderMap);

            LinkedMultiValueMap<String, Object> body = createBody(fileHttpEntity);

            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<FileTransferApiResponse> apiResponse = restTemplate.postForEntity(fileTransferApiURL, requestEntity, FileTransferApiResponse.class);

            fileUploaderResponse = createResponse(apiResponse);

        } catch (HttpClientErrorException | HttpServerErrorException httpEx) {
            logger.info(httpEx.getMessage());
            fileUploaderResponse = createErrorResponse(httpEx.getStatusCode());
        } catch (Exception e) {
            logger.error(e);
            fileUploaderResponse = createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return fileUploaderResponse;
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add(HEADER_API_KEY, fileTransferApiKey);
        return headers;
    }

    private LinkedMultiValueMap<String, String> createFileHeader(MultipartFile fileToUpload) {
        LinkedMultiValueMap<String, String> fileHeaderMap = new LinkedMultiValueMap<>();
        fileHeaderMap.add(HttpHeaders.CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_VALUE, UPLOAD, fileToUpload.getOriginalFilename()));
        return fileHeaderMap;
    }

    private LinkedMultiValueMap<String, Object> createBody(HttpEntity<byte[]> fileHttpEntity) {
        LinkedMultiValueMap<String, Object> multipartReqMap = new LinkedMultiValueMap<>();
        multipartReqMap.add(UPLOAD, fileHttpEntity);
        return multipartReqMap;
    }

    private FileUploaderResponse createResponse(ResponseEntity<FileTransferApiResponse> response) {
        FileUploaderResponse fileUploaderResponse = new FileUploaderResponse();
        if (response != null) {
            fileUploaderResponse.setHttpStatus(response.getStatusCode());
            FileTransferApiResponse apiResponse = response.getBody();
            if (apiResponse != null) {
                fileUploaderResponse.setFileId(apiResponse.getId());
            }
        } else {
            logger.error("null response from file transfer api url " + fileTransferApiURL);
            fileUploaderResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return fileUploaderResponse;
    }

    private FileUploaderResponse createErrorResponse(HttpStatus httpStatus) {
        FileUploaderResponse fileUploaderResponse = new FileUploaderResponse();
        fileUploaderResponse.setHttpStatus(httpStatus);
        return fileUploaderResponse;
    }
}
