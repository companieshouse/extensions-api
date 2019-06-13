package uk.gov.companieshouse.extensions.api.attachments.file;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.logger.LogMethodCall;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

/**
 * Client for using the File-Transfer-Api for upload / download / delete of files
 */
@Component
public class FileTransferApiClient {

    private static final String DOWNLOAD_URI = "%s/%s/download";
    private static final String HEADER_API_KEY = "x-api-key";
    private static final String UPLOAD = "upload";
    private static final String CONTENT_DISPOSITION_VALUE = "form-data; name=%s; filename=%s";
    private static final String NULL_RESPONSE_MESSAGE = "null response from file transfer api url";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    @Autowired
    private ApiLogger logger;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${FILE_TRANSFER_API_URL}")
    private String fileTransferApiURL;

    @Value("${FILE_TRANSFER_API_KEY}")
    private String fileTransferApiKey;


    private <T> FileTransferApiClientResponse makeApiCall(FileTransferOperation <T> operation,
                                                          FileTransferResponseBuilder <T> responseBuilder) {
        FileTransferApiClientResponse response = new FileTransferApiClientResponse();

        try {
             T operationResponse = operation.execute();

             response = responseBuilder.createResponse(operationResponse);
        } catch (IOException e) {
            logger.error(e);
            response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    /**
     * Downloads a file from the file-transfer-api
     * The RestTemplate execute method takes a callback function to handle the response
     * from the file-transfer-api. it's in here that we copy the data coming in from
     * the file-transfer-api into the provided outputStream.
     * @param fileId The id used by the file-transfer-api to identify the file
     * @param httpServletResponse The HttpServletResponse to stream the file to
     * @return FileTransferApiClientResponse containing the http status
     */
    @LogMethodCall
    public FileTransferApiClientResponse download(String fileId, HttpServletResponse httpServletResponse) {
        String downloadUri = String.format(DOWNLOAD_URI, fileTransferApiURL, fileId);

        return makeApiCall(
            //FileTransferOperation
            () -> restTemplate.execute(
                downloadUri,
                HttpMethod.GET,
                requestCallback -> {
                    requestCallback.getHeaders().setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                    requestCallback.getHeaders().add(HEADER_API_KEY, fileTransferApiKey);
                },
                clientHttpResponse -> {
                    setResponseHeaders(httpServletResponse, clientHttpResponse);

                    InputStream inputStream = clientHttpResponse.getBody();
                    IOUtils.copy(inputStream, httpServletResponse.getOutputStream());
                    return clientHttpResponse;
                }),

            //FileTransferResponseBuilder
            clientHttpResponse -> {
                FileTransferApiClientResponse fileTransferApiClientResponse = new FileTransferApiClientResponse();
                if (clientHttpResponse != null) {
                    fileTransferApiClientResponse.setHttpStatus(clientHttpResponse.getStatusCode());
                } else {
                    logger.error(NULL_RESPONSE_MESSAGE + " " + fileTransferApiURL);
                    fileTransferApiClientResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                return fileTransferApiClientResponse;
            }
        );
    }

    /**
     * Copies file detail headers returned from the file-transfer-api call into the httpServletResponse
     * @param httpServletResponse response to stream file to
     * @param clientHttpResponse the response back from the api we are calling - the file-transfer-api
     */
    private void setResponseHeaders(HttpServletResponse httpServletResponse, ClientHttpResponse clientHttpResponse) {
        HttpHeaders incomingHeaders = clientHttpResponse.getHeaders();
        if (incomingHeaders != null) {
            MediaType contentType = incomingHeaders.getContentType();
            if (contentType != null) {
                httpServletResponse.setHeader(CONTENT_TYPE, contentType.toString());
            }
            httpServletResponse.setHeader(CONTENT_LENGTH, String.valueOf(incomingHeaders.getContentLength()));
            httpServletResponse.setHeader(CONTENT_DISPOSITION, incomingHeaders.getContentDisposition().toString());
        }
    }

    /**
     * Uploads a file to the file-transfer-api
     * Creates a multipart form request containing the file and sends to
     * the file-transfer-api. The response from the file-transfer-api contains
     * the new unique id for the file. This is captured and returned in the FileTransferApiClientResponse.
     * @param fileToUpload The file to upload
     * @return FileTransferApiClientResponse containing the file id if successful, and http status
     */
    @LogMethodCall
    public FileTransferApiClientResponse upload(MultipartFile fileToUpload) {
        return makeApiCall(
            //FileTransferOperation
            () -> {
                HttpHeaders headers = createUploadHttpHeaders();
                LinkedMultiValueMap<String, String> fileHeaderMap = createUploadFileHeader(fileToUpload);
                HttpEntity<byte[]> fileHttpEntity = new HttpEntity<>(fileToUpload.getBytes(), fileHeaderMap);
                LinkedMultiValueMap<String, Object> body = createUploadBody(fileHttpEntity);
                HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
                return restTemplate.postForEntity(fileTransferApiURL, requestEntity, FileTransferApiResponse.class);
            },

            //FileTransferResponseBuilder
            responseEntity -> {
                FileTransferApiClientResponse fileTransferApiClientResponse = new FileTransferApiClientResponse();
                if (responseEntity != null) {
                    fileTransferApiClientResponse.setHttpStatus(responseEntity.getStatusCode());
                    FileTransferApiResponse apiResponse = responseEntity.getBody();
                    if (apiResponse != null) {
                        fileTransferApiClientResponse.setFileId(apiResponse.getId());
                    }
                } else {
                    logger.error(NULL_RESPONSE_MESSAGE + " " + fileTransferApiURL);
                    fileTransferApiClientResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                return fileTransferApiClientResponse;
            }
        );
    }

    private HttpHeaders createUploadHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add(HEADER_API_KEY, fileTransferApiKey);
        return headers;
    }

    private LinkedMultiValueMap<String, String> createUploadFileHeader(MultipartFile fileToUpload) {
        LinkedMultiValueMap<String, String> fileHeaderMap = new LinkedMultiValueMap<>();
        fileHeaderMap.add(HttpHeaders.CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_VALUE, UPLOAD, fileToUpload.getOriginalFilename()));
        return fileHeaderMap;
    }

    private LinkedMultiValueMap<String, Object> createUploadBody(HttpEntity<byte[]> fileHttpEntity) {
        LinkedMultiValueMap<String, Object> multipartReqMap = new LinkedMultiValueMap<>();
        multipartReqMap.add(UPLOAD, fileHttpEntity);
        return multipartReqMap;
    }
}
