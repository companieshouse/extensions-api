package uk.gov.companieshouse.extensions.api.attachments.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUploader {

    @Value("${FILE_TRANSFER_API_URL}")
    private String fileTransferApiURL;

    @Value("${FILE_TRANSFER_API_KEY}")
    private String fileTransferApiKey;

    private static final String HEADER_API_KEY = "x-api-key";
    private static final String FILE_TRANSFER_API_FIELD_NAME = "upload";
    private static final String HEADER_CONTENT_DISPOSITION = "Content-disposition";
    private static final String CONTENT_DISPOSITION_VALUE = "form-data; name=%s; filename=%s";

    public FileUploaderResponse upload(MultipartFile fileToUpload) {

        FileUploaderResponse fileUploaderResponse;

        HttpHeaders headers = getHttpHeaders();

        LinkedMultiValueMap<String, String> fileHeaderMap = getFileHeader(fileToUpload);

        try {
            HttpEntity<byte[]> fileHttpEntity = new HttpEntity<>(fileToUpload.getBytes(), fileHeaderMap);

            LinkedMultiValueMap<String, Object> body = getBody(fileHttpEntity);

            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<FileTransferApiResponse> apiResponse = restTemplate.postForEntity(fileTransferApiURL, requestEntity, FileTransferApiResponse.class);

            fileUploaderResponse = getResponse(apiResponse);
        } catch (HttpClientErrorException | HttpServerErrorException httpEx) {
            //TODO log
            fileUploaderResponse = getResponse(httpEx);
            fileUploaderResponse.setErrorStatusCode(String.valueOf(httpEx.getRawStatusCode()));
            fileUploaderResponse.setErrorStatusText(httpEx.getStatusText());
        } catch (Exception e) {
            //TODO log
            fileUploaderResponse = getResponse(e);
        }
        return fileUploaderResponse;
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

    private FileUploaderResponse getResponse(Exception e) {
        FileUploaderResponse fileUploaderResponse = new FileUploaderResponse();
        fileUploaderResponse.setInError(true);
        fileUploaderResponse.setErrorMessage(e.getMessage());
        return fileUploaderResponse;
    }

    private FileUploaderResponse getResponse(ResponseEntity<FileTransferApiResponse> response) {
        FileUploaderResponse fileUploaderResponse = new FileUploaderResponse();
        if (response.getStatusCode().isError()) {
            fileUploaderResponse.setInError(true);
            fileUploaderResponse.setErrorMessage(response.getStatusCode().toString());
        } else {
            fileUploaderResponse.setInError(false);
            FileTransferApiResponse apiResponse = response.getBody();
            if (apiResponse != null) {
                fileUploaderResponse.setFileId(apiResponse.getId());
            }
        }
        return fileUploaderResponse;
    }
}
