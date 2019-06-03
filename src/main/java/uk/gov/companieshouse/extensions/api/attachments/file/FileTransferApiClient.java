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


    private <R, T> R makeApiCall(FileTransferOperation <T> operation,
                                 FileTransferResponseBuilder <R,T> responseBuilder,
                                 FileTransferErrorResponseBuilder <R> errorResponseBuilder) {
        R result;
        try {
             T restResponse = operation.execute();

             result = responseBuilder.createResponse(restResponse);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.info(e.getMessage());
            result = errorResponseBuilder.createErrorResponse(e.getStatusCode());
        } catch (Exception e) {
            logger.error(e);
            result = errorResponseBuilder.createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return result;
    }

    @LogMethodCall
    public FileDownloaderResponse download(String fileId, OutputStream outputStream) {
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
                FileDownloaderResponse fileDownloaderResponse = new FileDownloaderResponse();
                if (clientHttpResponse != null) {
                    fileDownloaderResponse.setHttpStatus(clientHttpResponse.getStatusCode());
                    fileDownloaderResponse.setHttpHeaders(clientHttpResponse.getHeaders());
                } else {
                    logger.error(NULL_RESPONSE_MESSAGE + fileTransferApiURL);
                    fileDownloaderResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                return fileDownloaderResponse;
            },

            httpStatus -> {
                FileDownloaderResponse fileDownloaderResponse = new FileDownloaderResponse();
                fileDownloaderResponse.setHttpStatus(httpStatus);
                return fileDownloaderResponse;
            }
        );
    }

    @LogMethodCall
    public FileUploaderResponse upload(MultipartFile fileToUpload) {
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
                FileUploaderResponse fileUploaderResponse = new FileUploaderResponse();
                if (responseEntity != null) {
                    fileUploaderResponse.setHttpStatus(responseEntity.getStatusCode());
                    FileTransferApiResponse apiResponse = responseEntity.getBody();
                    if (apiResponse != null) {
                        fileUploaderResponse.setFileId(apiResponse.getId());
                    }
                } else {
                    logger.error(NULL_RESPONSE_MESSAGE + fileTransferApiURL);
                    fileUploaderResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                return fileUploaderResponse;
            },

            httpStatus -> {
                FileUploaderResponse fileUploaderResponse = new FileUploaderResponse();
                fileUploaderResponse.setHttpStatus(httpStatus);
                return fileUploaderResponse;
            }
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
