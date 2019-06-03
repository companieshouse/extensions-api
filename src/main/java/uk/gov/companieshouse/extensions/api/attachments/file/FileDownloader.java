package uk.gov.companieshouse.extensions.api.attachments.file;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.logger.LogMethodCall;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

@Component
public class FileDownloader {

    @Autowired
    private ApiLogger logger;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${FILE_TRANSFER_API_URL}")
    private String fileTransferApiURL;

    @Value("${FILE_TRANSFER_API_KEY}")
    private String fileTransferApiKey;

    private static final String DOWNLOAD_URI = "%s/%s/download";
    private static final String HEADER_API_KEY = "x-api-key";

    /**
     * Downloads a file from the File Transfer API
     * @param fileId ID of file used by the File Transfer API that we want to get
     * @param outputStream the Http response stream to stream the file out to
     */
    @LogMethodCall
    public FileDownloaderResponse download(String fileId, OutputStream outputStream) {
        String downloadUri = String.format(DOWNLOAD_URI, fileTransferApiURL, fileId);
        logger.debug("Download URI = " + downloadUri);

        FileDownloaderResponse fileDownloaderResponse;

        try {
            ClientHttpResponse apiResponse = restTemplate.execute(
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
                }
            );

            fileDownloaderResponse = getResponse(apiResponse);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.info(e.getMessage());
            fileDownloaderResponse = getErrorResponse(e.getStatusCode());
        } catch (Exception e) {
            logger.error(e);
            fileDownloaderResponse = getErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return fileDownloaderResponse;
    }

    private FileDownloaderResponse getResponse(ClientHttpResponse apiResponse) throws IOException {
        FileDownloaderResponse fileDownloaderResponse = new FileDownloaderResponse();
        if (apiResponse != null) {
            fileDownloaderResponse.setHttpStatus(apiResponse.getStatusCode());
            fileDownloaderResponse.setHttpHeaders(apiResponse.getHeaders());
        } else {
            logger.error("null response from file transfer api url " + fileTransferApiURL);
            fileDownloaderResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return fileDownloaderResponse;
    }

    private FileDownloaderResponse getErrorResponse(HttpStatus statusCode) {
        FileDownloaderResponse fileDownloaderResponse = new FileDownloaderResponse();
        fileDownloaderResponse.setHttpStatus(statusCode);
        return fileDownloaderResponse;
    }
}
