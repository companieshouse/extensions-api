package uk.gov.companieshouse.extensions.api.attachments.file;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.logger.LogMethodCall;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

@Component
public class FileTransferApiClient {

    private static final String DOWNLOAD_URI = "%s/%s/download";
    private static final String HEADER_API_KEY = "x-api-key";
    private static final String UPLOAD = "upload";
    private static final String CONTENT_DISPOSITION_VALUE = "form-data; name=%s; filename=%s";
    private static final String NULL_RESPONSE_MESSAGE = "null response from file transfer api url ";

    @Autowired
    private ApiLogger logger;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${FILE_TRANSFER_API_URL}")
    private String fileTransferApiURL;

    @Value("${FILE_TRANSFER_API_KEY}")
    private String fileTransferApiKey;


    private <R extends ApiClientResponse, T> R makeApiCall(FileTransferOperation <T> operation,
                                                           FileTransferResponseBuilder <R,T> responseBuilder,
                                                           R errorResponse) {
        R result;
        try {
             T restResponse = operation.execute();

             result = responseBuilder.createResponse(restResponse);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.info(e.getMessage());
            errorResponse.setHttpStatus(e.getStatusCode());
            result = errorResponse;
        } catch (Exception e) {
            logger.error(e);
            errorResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            result = errorResponse;
        }
        return result;
    }

    /**
     * Downloads a file from the file-transfer-api
     * The RestTemplate execute method takes a callback function to handle the response
     * from the file-transfer-api. it's in here that we copy the data coming in from
     * the file-transfer-api into the provided outputStream.
     * @param fileId The id used by the file-transfer-api to identify the file
     * @param outputStream The outputStream to capture the file
     * @return DownloadResponse containing the response data
     */
    @LogMethodCall
    public DownloadResponse download(String fileId, OutputStream outputStream) {
        String downloadUri = String.format(DOWNLOAD_URI, fileTransferApiURL, fileId);

        return makeApiCall(
            () -> restTemplate.execute(
                downloadUri,
                HttpMethod.GET,
                requestCallback -> {
                    requestCallback.getHeaders().setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                    requestCallback.getHeaders().add(HEADER_API_KEY, fileTransferApiKey);
                },
                clientHttpResponse -> {
                    InputStream inputStream = clientHttpResponse.getBody();
                    IOUtils.copy(inputStream, outputStream);
                    return clientHttpResponse;
                }),

            clientHttpResponse -> {
                DownloadResponse downloadResponse = new DownloadResponse();
                if (clientHttpResponse != null) {
                    downloadResponse.setHttpStatus(clientHttpResponse.getStatusCode());
                    downloadResponse.setHttpHeaders(clientHttpResponse.getHeaders());
                } else {
                    logger.error(NULL_RESPONSE_MESSAGE + fileTransferApiURL);
                    downloadResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                return downloadResponse;
            },

            new DownloadResponse()
        );
    }

    /**
     * Uploads a file to the file-transfer-api
     * Creates a multipart form request containing the file and sends to
     * the file-transfer-api. The response from the file-transfer-api contains
     * the new unique id for the file. This is captured and returned in the UploadResponse.
     * @param fileToUpload The file to upload
     * @return UploadResponse containing the file id if successful, error info if not
     */
    @LogMethodCall
    public UploadResponse upload(MultipartFile fileToUpload) {
        return makeApiCall(
            () -> {
                HttpHeaders headers = createHttpHeaders();
                LinkedMultiValueMap<String, String> fileHeaderMap = createFileHeader(fileToUpload);
                HttpEntity<byte[]> fileHttpEntity = new HttpEntity<>(fileToUpload.getBytes(), fileHeaderMap);
                LinkedMultiValueMap<String, Object> body = createBody(fileHttpEntity);
                HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
                return restTemplate.postForEntity(fileTransferApiURL, requestEntity, FileTransferApiResponse.class);
            },

            responseEntity -> {
                UploadResponse uploadResponse = new UploadResponse();
                if (responseEntity != null) {
                    uploadResponse.setHttpStatus(responseEntity.getStatusCode());
                    FileTransferApiResponse apiResponse = responseEntity.getBody();
                    if (apiResponse != null) {
                        uploadResponse.setFileId(apiResponse.getId());
                    }
                } else {
                    logger.error(NULL_RESPONSE_MESSAGE + fileTransferApiURL);
                    uploadResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                return uploadResponse;
            },

            new UploadResponse()
        );
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
}
