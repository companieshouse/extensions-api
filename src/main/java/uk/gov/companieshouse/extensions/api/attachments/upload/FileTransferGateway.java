package uk.gov.companieshouse.extensions.api.attachments.upload;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.logger.LogMethodCall;

@Component
public class FileTransferGateway {

    @Autowired
    private ApiLogger logger;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${FILE_TRANSFER_API_URL}")
    private String fileTransferApiURL;

    @Value("${FILE_TRANSFER_API_KEY}")
    private String fileTransferApiKey;

    private static final String HEADER_API_KEY = "x-api-key";
    private static final String FILE_TRANSFER_API_FIELD_NAME = "upload";
    private static final String HEADER_CONTENT_DISPOSITION = "Content-disposition";
    private static final String CONTENT_DISPOSITION_VALUE = "form-data; name=%s; filename=%s";
    private static final String DOWNLOAD_URI = "%s/%s/download";

    @LogMethodCall
    public FileTransferGatewayResponse upload(MultipartFile fileToUpload) {

        FileTransferGatewayResponse fileTransferGatewayResponse;

        HttpHeaders headers = getHttpHeaders();

        LinkedMultiValueMap<String, String> fileHeaderMap = getFileHeader(fileToUpload);

        try {
            HttpEntity<byte[]> fileHttpEntity = new HttpEntity<>(fileToUpload.getBytes(), fileHeaderMap);

            LinkedMultiValueMap<String, Object> body = getBody(fileHttpEntity);

            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<FileTransferApiResponse> apiResponse = restTemplate.postForEntity(fileTransferApiURL, requestEntity, FileTransferApiResponse.class);

            fileTransferGatewayResponse = getResponse(apiResponse);
        } catch (IOException e) {
            logger.error(e);
            fileTransferGatewayResponse = getResponse(e);
        }
        return fileTransferGatewayResponse;
    }

    @LogMethodCall
    public void download(String fileId, OutputStream outputStream) {
        String downloadUri = String.format(DOWNLOAD_URI, fileTransferApiURL, fileId);

        restTemplate.execute(
            downloadUri,
            HttpMethod.GET,
            requestCallback -> {
                requestCallback.getHeaders().setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                requestCallback.getHeaders().add(HEADER_API_KEY, fileTransferApiKey);
            },
            responseExtractor -> IOUtils.copy(responseExtractor.getBody(), outputStream)
        );
        //TODO - error handling
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add(HEADER_API_KEY, fileTransferApiKey);
        return headers;
    }

    private LinkedMultiValueMap<String, String> getFileHeader(MultipartFile fileToUpload) {
        LinkedMultiValueMap<String, String> fileHeaderMap = new LinkedMultiValueMap<>();
        fileHeaderMap.add(HEADER_CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_VALUE, FILE_TRANSFER_API_FIELD_NAME, fileToUpload.getOriginalFilename()));
        return fileHeaderMap;
    }

    private LinkedMultiValueMap<String, Object> getBody(HttpEntity<byte[]> fileHttpEntity) {
        LinkedMultiValueMap<String, Object> multipartReqMap = new LinkedMultiValueMap<>();
        multipartReqMap.add(FILE_TRANSFER_API_FIELD_NAME, fileHttpEntity);
        return multipartReqMap;
    }

    private FileTransferGatewayResponse getResponse(Exception e) {
        FileTransferGatewayResponse fileTransferGatewayResponse = new FileTransferGatewayResponse();
        fileTransferGatewayResponse.setInError(true);
        fileTransferGatewayResponse.setErrorMessage(e.getMessage());
        return fileTransferGatewayResponse;
    }

    private FileTransferGatewayResponse getResponse(ResponseEntity<FileTransferApiResponse> response) {
        FileTransferGatewayResponse fileTransferGatewayResponse = new FileTransferGatewayResponse();
        if (response.getStatusCode().isError()) {
            fileTransferGatewayResponse.setInError(true);
            fileTransferGatewayResponse.setErrorMessage(response.getStatusCode().toString());
        } else {
            fileTransferGatewayResponse.setInError(false);
            FileTransferApiResponse apiResponse = response.getBody();
            if (apiResponse != null) {
                fileTransferGatewayResponse.setFileId(apiResponse.getId());
            }
        }
        return fileTransferGatewayResponse;
    }
}
