package uk.gov.companieshouse.extensions.api.attachments.file;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.filetransfer.FileApi;
import uk.gov.companieshouse.api.model.filetransfer.IdApi;
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

    @Autowired
    private ApiLogger logger;
    @Autowired
    private Supplier<InternalApiClient> internalApiClientSupplier;

    @Autowired
    private Tika tika;


    /**
     * Downloads a file from the file-transfer-service
     * The private-api-sdk callback handle the response
     * from the file-transfer-service. it's in here that we copy the data coming in from
     * the file-transfer-service into the provided outputStream.
     * @param fileId The id used by the file-transfer-service to identify the file
     * @param httpServletResponse The HttpServletResponse to stream the file to
     * @return FileTransferApiClientResponse containing the http status
     */
    @LogMethodCall
    public void download(String fileId, HttpServletResponse httpServletResponse) {

        ApiResponse<byte[]> downloadResponse = null;

        var fileTransferApiClientResponse = new FileTransferApiClientResponse();
        try {
            downloadResponse = downloadFileAsBinary(fileId);
        } catch (URIValidationException e) {
            logger.error(URI_VALIDATION_FAILED_MESSAGE + " " + DOWNLOAD);
            fileTransferApiClientResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ApiErrorResponseException e) {
            logger.error(API_ERROR_RESPONSE_MESSAGE + " " + DOWNLOAD + ": %s".formatted(Arrays.toString(e.getStackTrace())));
            throw new HttpServerErrorException(HttpStatus.valueOf(e.getStatusCode()));
        }

        if (downloadResponse != null) {
            setResponseHeaders(httpServletResponse, downloadResponse);

            try (OutputStream os = httpServletResponse.getOutputStream()) {
                os.write(downloadResponse.getData(), 0, downloadResponse.getData().length);
                os.flush();
                logger.debug("fileId " + fileId + " downloaded successfully");
                logger.debug("file size is " + downloadResponse.getData().length);
            } catch (IOException e) {
                logger.error(IO_EXCEPTION_MESSAGE + " " + DOWNLOAD);
                httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        } else {
            logger.error(NULL_RESPONSE_MESSAGE + " " + DOWNLOAD);
            httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * Uploads a file to the file-transfer-service
     * Creates a multipart form request containing the file and sends to
     * the file-transfer-service. The response from the file-transfer-service contains
     * the new unique id for the file. This is captured and returned in the FileTransferApiClientResponse.
     *
     * @param fileToUpload The file to upload
     * @return FileTransferApiClientResponse containing the file id if successful, and http status
     */
    @LogMethodCall
    public FileTransferApiClientResponse upload(MultipartFile fileToUpload) {
        var fileTransferApiClientResponse = new FileTransferApiClientResponse();
        var originalFilename = fileToUpload.getOriginalFilename();
        String fileType;
        try {
            fileType = tika.detect(fileToUpload.getInputStream(), originalFilename);
        } catch (IOException e) {
            logger.error(IO_EXCEPTION_MESSAGE + " " + UPLOAD);
            fileTransferApiClientResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return fileTransferApiClientResponse;
        }
        String extension = getFileExtension(originalFilename);
        if (!MimeTypeValidator.isValidMimeType(fileType)) {
            throw new HttpClientErrorException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
        FileApi fileApi;
        try {
            fileApi = new FileApi(originalFilename, fileToUpload.getBytes(), fileType, (int) fileToUpload.getSize(), extension);
            logger.info("file details for upload" + fileApi.getMimeType() + " " + originalFilename);
        } catch (IOException e) {
            logger.error(IO_EXCEPTION_MESSAGE + " " + UPLOAD);
            fileTransferApiClientResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return fileTransferApiClientResponse;
        }

        ApiResponse<IdApi> uploadResponse;
        try {
            uploadResponse = uploadFile(fileApi);
        } catch (URIValidationException e) {
            logger.error(URI_VALIDATION_FAILED_MESSAGE + " " + UPLOAD);
            fileTransferApiClientResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return fileTransferApiClientResponse;
        } catch (ApiErrorResponseException e) {
            logger.error(API_ERROR_RESPONSE_MESSAGE + " " + UPLOAD + " " + e.getStatusCode());
            throw new HttpServerErrorException(HttpStatus.valueOf(e.getStatusCode()));
        }

        if (uploadResponse != null) {
            logger.info("upload response file details " + uploadResponse.getStatusCode() + " " + originalFilename);
            fileTransferApiClientResponse.setHttpStatus(HttpStatus.valueOf(uploadResponse.getStatusCode()));
            IdApi apiResponse = uploadResponse.getData();
            if (apiResponse != null) {
                fileTransferApiClientResponse.setFileId(apiResponse.getId());
            }
        } else {
            logger.error(NULL_RESPONSE_MESSAGE + " " + UPLOAD);
            fileTransferApiClientResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return fileTransferApiClientResponse;
    }


    /**
     * Delete a file from S3 via the file-transfer-service
     * @param fileId of the file to delete
     * @return FileTransferApiClientResponse containing the http status
     */
    public FileTransferApiClientResponse delete(String fileId) {
        ApiResponse<Void> deleteResponse;
        var fileTransferApiClientResponse = new FileTransferApiClientResponse();
        try {
            deleteResponse = deleteFile(fileId);
        } catch (URIValidationException e) {
            logger.error(URI_VALIDATION_FAILED_MESSAGE + " " + DELETE);
            fileTransferApiClientResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return fileTransferApiClientResponse;
        } catch (ApiErrorResponseException e) {
            logger.error(API_ERROR_RESPONSE_MESSAGE + " " + DELETE);
            throw new HttpServerErrorException(HttpStatus.valueOf(e.getStatusCode()));
        }
        if (deleteResponse != null) {
            fileTransferApiClientResponse.setHttpStatus(HttpStatus.valueOf(deleteResponse.getStatusCode()));
        } else {
            logger.error(NULL_RESPONSE_MESSAGE + " " + DELETE);
            fileTransferApiClientResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return fileTransferApiClientResponse;
    }

    private void setResponseHeaders(HttpServletResponse httpServletResponse, ApiResponse<byte[]> clientHttpResponse) {
        Map<String, Object> incomingHeaders = clientHttpResponse.getHeaders();
        MediaType contentType = (MediaType) incomingHeaders.get(CONTENT_TYPE);
        if (contentType != null) {
            httpServletResponse.setHeader(CONTENT_TYPE, contentType.toString());
        }
        httpServletResponse.setHeader(CONTENT_LENGTH, String.valueOf(incomingHeaders.get(CONTENT_LENGTH)));
        httpServletResponse.setHeader(CONTENT_DISPOSITION, incomingHeaders.get(CONTENT_DISPOSITION).toString());
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }


    private ApiResponse<IdApi> uploadFile(final FileApi fileApi) throws ApiErrorResponseException, URIValidationException {
        return internalApiClientSupplier.get().privateFileTransferResourceHandler().upload(fileApi).execute();
    }

    private ApiResponse<byte[]> downloadFileAsBinary(final String fileId) throws ApiErrorResponseException, URIValidationException {
        return internalApiClientSupplier.get().privateFileTransferResourceHandler()
            .downloadBinary(fileId)
            .execute();
    }

    private ApiResponse<Void> deleteFile(final String fileId) throws ApiErrorResponseException, URIValidationException {
        return internalApiClientSupplier.get().privateFileTransferResourceHandler()
            .delete(fileId)
            .execute();
    }

}
