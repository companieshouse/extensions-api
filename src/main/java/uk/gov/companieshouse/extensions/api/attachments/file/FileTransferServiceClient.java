package uk.gov.companieshouse.extensions.api.attachments.file;

import static java.lang.String.format;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;
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
    public void download(final String fileId, final HttpServletResponse httpServletResponse) {
        logger.info(format("download(fileId=%s) method called.", fileId));
        try {
            InternalFileTransferClient internalFileTransferClient = fileTransferClientSupplier.get();

            ApiResponse<FileApi> response = internalFileTransferClient.privateFileTransferHandler()
                .download(fileId)
                .execute();

            logger.info(format("Download Complete[%d]: (Headers Available=%s)", response.getStatusCode(), response.getHeaders()));

            setDownloadResponseHeaders(httpServletResponse, response);
            buildDownloadResponse(httpServletResponse, response.getData());

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
    public FileTransferApiClientResponse upload(final MultipartFile fileToUpload) {
        logger.info(format("upload(file=%s) method called.", fileToUpload.getOriginalFilename()));
        try {
            InputStream fileStream = fileToUpload.getInputStream();
            String contentType = fileToUpload.getContentType();
            String filename = fileToUpload.getOriginalFilename();

            InternalFileTransferClient internalFileTransferClient = fileTransferClientSupplier.get();

            ApiResponse<IdApi> uploadResponse =  internalFileTransferClient.privateFileTransferHandler()
                .upload(fileStream, contentType, filename)
                .execute();

            FileTransferApiClientResponse response = new FileTransferApiClientResponse();
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
    @LogMethodCall
    public FileTransferApiClientResponse delete(final String fileId) {
        logger.info(format("delete(fileId=%s) method called.", fileId));
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

    @LogMethodCall
    private void setDownloadResponseHeaders(final HttpServletResponse servletResponse, final ApiResponse<FileApi> apiResponse) {
        logger.info(format("setDownloadResponseHeaders(headers=%d) method called.", apiResponse.getHeaders().size()));

        // Define the headers we want to copy from the response.
        Map<String, Object> incomingHeaders = apiResponse.getHeaders();

        // Check the content-type is available and set it in the response.
        Object contentType = incomingHeaders.get(CONTENT_TYPE);
        if(contentType != null) {
            logger.debug(format("Content-Type is available: %s", contentType));
            servletResponse.setHeader(CONTENT_TYPE, contentType.toString());
        } else {
            logger.debug(format("Content-Type is NOT available in the response headers, using body data: %s", apiResponse.getData().getMimeType()));
            servletResponse.setHeader(CONTENT_TYPE, apiResponse.getData().getMimeType());
        }

        Object contentLength = incomingHeaders.get(CONTENT_LENGTH);
        if(contentLength != null) {
            logger.debug(format("Content-Length is available: %s", contentLength));
            servletResponse.setHeader(CONTENT_LENGTH, String.valueOf(contentLength));
        } else {
            logger.debug(format("Content-Length is NOT available in the response headers, using body data: %d byte(s)", apiResponse.getData().getBody().length));
            servletResponse.setHeader(CONTENT_LENGTH, String.valueOf(apiResponse.getData().getSize()));
        }

        // Set the content-disposition header if it exists in the incoming headers.
        Object contentDisposition = incomingHeaders.get(CONTENT_DISPOSITION);
        if(contentDisposition != null) {
            logger.debug(format("Content-Disposition is available: %s", contentDisposition));
            servletResponse.setHeader(CONTENT_DISPOSITION, contentDisposition.toString());
        } else {
            logger.debug(format("Content-Disposition is NOT available in the response headers, using body data: %s", apiResponse.getData().getFileName()));
            servletResponse.setHeader(CONTENT_DISPOSITION, format("attachment; filename=\"%s\"", apiResponse.getData().getFileName()));
        }
    }

    private void buildDownloadResponse(final HttpServletResponse httpServletResponse, final FileApi fileApi) {
        logger.info(format("buildDownloadResponse(filename=%s) method called.", fileApi.getFileName()));

        try (OutputStream os = httpServletResponse.getOutputStream()) {
            logger.debug(format("Attempting to write data to output stream: %s byte(s) available...", fileApi.getBody().length));
            logger.debug(format("Data integrity check > (File Size: %d, Content Length: %d)",
                fileApi.getSize(), fileApi.getBody().length));

            httpServletResponse.setStatus(HttpStatus.OK.value());

            os.write(fileApi.getBody(), 0, fileApi.getSize());
            os.flush();

        } catch (IOException e) {
            logger.error(IO_EXCEPTION_MESSAGE + " " + DOWNLOAD);
            httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

}
