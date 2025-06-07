package uk.gov.companieshouse.extensions.api.attachments.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.filetransfer.FileApi;
import uk.gov.companieshouse.api.filetransfer.IdApi;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.filetransfer.InternalFileTransferClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.logger.LogMethodCall;

/**
 * Client for using the file-transfer-service for upload / download / delete of files
 */
@Component
public class FileTransferServiceClient {

    private static final String DOWNLOAD = "download";
    private static final String NULL_RESPONSE_MESSAGE = "null response from file transfer api url";
    private static final String URI_VALIDATION_FAILED_MESSAGE = "uri validation failed from file transfer service url";
    private static final String API_ERROR_RESPONSE_MESSAGE = "Api Error Response from file transfer service url";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String UPLOAD = "UPLOAD";
    private static final String DELETE = "DELETE";
    private static final String IO_EXCEPTION_MESSAGE = "IO exception occurred from file transfer service url";

    private final ApiLogger logger;
    private final Supplier<InternalFileTransferClient> fileTransferClientSupplier;

    public FileTransferServiceClient(ApiLogger logger, Supplier<InternalFileTransferClient> fileTransferClientSupplier) {
        this.logger = logger;
        this.fileTransferClientSupplier = fileTransferClientSupplier;
    }

    @LogMethodCall
    public void download(String fileId, HttpServletResponse httpServletResponse) {

        try {
            InternalFileTransferClient internalFileTransferClient = fileTransferClientSupplier.get();
            ApiResponse<FileApi> response = internalFileTransferClient.privateFileTransferHandler()
                .download(fileId)
                .execute();

            if (response != null) {
                setResponseHeaders(httpServletResponse, response);

                try (OutputStream os = httpServletResponse.getOutputStream()) {
                    os.write(response.getData().getBody(), 0, response.getData().getSize());
                    os.flush();
                    logger.debug("fileId " + fileId + " downloaded successfully");
                    logger.debug("file size is " + response.getData().getSize());
                } catch (IOException e) {
                    logger.error(IO_EXCEPTION_MESSAGE + " " + DOWNLOAD);
                    httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                }
            } else {
                logger.error(NULL_RESPONSE_MESSAGE + " " + DOWNLOAD);
                httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        } catch (URIValidationException e) {
            logger.error(URI_VALIDATION_FAILED_MESSAGE + " " + DOWNLOAD);
            throw new FileTransferURIValidationException(URI_VALIDATION_FAILED_MESSAGE, e);
        } catch (ApiErrorResponseException e) {
            logger.error(API_ERROR_RESPONSE_MESSAGE + " " + DOWNLOAD + ": %s".formatted(Arrays.toString(e.getStackTrace())));
            throw new HttpServerErrorException(HttpStatus.valueOf(e.getStatusCode()));
        } catch (Exception e) {
            logger.error(API_ERROR_RESPONSE_MESSAGE + " " + DOWNLOAD, e);
            httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @LogMethodCall
    public FileTransferApiClientResponse upload(MultipartFile fileToUpload) {
        try {
            InternalFileTransferClient internalFileTransferClient = fileTransferClientSupplier.get();
            InputStream fileStream = fileToUpload.getInputStream();
            String contentType = fileToUpload.getContentType();
            String filename = fileToUpload.getOriginalFilename();

            ApiResponse<IdApi> uploadResponse =  internalFileTransferClient.privateFileTransferHandler()
                .upload(fileStream, contentType, filename)
                .execute();
            FileTransferApiClientResponse response =new FileTransferApiClientResponse();
            if (uploadResponse != null) {
                response.httpStatus(HttpStatus.valueOf(uploadResponse.getStatusCode()));
                if (uploadResponse.getData() != null) {
                    response.fileId(uploadResponse.getData().getId());
                }
            } else {
                logger.error(NULL_RESPONSE_MESSAGE + " " + UPLOAD);
                response.httpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return response;
        } catch (URIValidationException e) {
            logger.error(URI_VALIDATION_FAILED_MESSAGE + " " + UPLOAD);
            throw new FileTransferURIValidationException(URI_VALIDATION_FAILED_MESSAGE, e);
        } catch (ApiErrorResponseException e) {
            logger.error(API_ERROR_RESPONSE_MESSAGE + " " + UPLOAD + " " + e.getStatusCode());
            throw new HttpServerErrorException(HttpStatus.valueOf(e.getStatusCode()));
        } catch (IOException e) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Delete a file from S3 via the file-transfer-service
     * @param fileId of the file to delete
     * @return FileTransferApiClientResponse containing the http status
     */
    public FileTransferApiClientResponse delete(String fileId) {

        try {
            InternalFileTransferClient ftsClient = fileTransferClientSupplier.get();

            ApiResponse<Void> apiResponse = ftsClient.privateFileTransferHandler()
                .delete(fileId)
                .execute();

            return new FileTransferApiClientResponse()
                .fileId(fileId)
                .httpStatus(apiResponse != null? HttpStatus.valueOf(apiResponse.getStatusCode()) :
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (URIValidationException e) {
            logger.error(URI_VALIDATION_FAILED_MESSAGE + " " + DELETE);
            return new FileTransferApiClientResponse()
                .fileId(fileId)
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ApiErrorResponseException e) {
            logger.error(API_ERROR_RESPONSE_MESSAGE + " " + DELETE);
            throw new HttpServerErrorException(HttpStatus.valueOf(e.getStatusCode()));
        }
    }

    private void setResponseHeaders(HttpServletResponse httpServletResponse, ApiResponse<?> clientHttpResponse) {
        Map<String, Object> incomingHeaders = clientHttpResponse.getHeaders();
        MediaType contentType = (MediaType) incomingHeaders.get(CONTENT_TYPE);
        if (contentType != null) {
            httpServletResponse.setHeader(CONTENT_TYPE, contentType.toString());
        }
        httpServletResponse.setHeader(CONTENT_LENGTH, String.valueOf(incomingHeaders.get(CONTENT_LENGTH)));
        httpServletResponse.setHeader(CONTENT_DISPOSITION, incomingHeaders.get(CONTENT_DISPOSITION).toString());
    }
}
