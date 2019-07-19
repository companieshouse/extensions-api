package uk.gov.companieshouse.extensions.api.Utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.attachments.Attachment;
import uk.gov.companieshouse.extensions.api.attachments.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionCreateReason;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonDTO;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;
import uk.gov.companieshouse.extensions.api.reasons.ReasonStatus;
import uk.gov.companieshouse.extensions.api.requests.CreatedBy;
import uk.gov.companieshouse.extensions.api.requests.ExtensionCreateRequest;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullDTO;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;
import uk.gov.companieshouse.extensions.api.requests.Status;
import uk.gov.companieshouse.service.links.Links;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Utils {
    public static final String ETAG = "etag";
    public static final String REQUEST_ID = "1234";
    public static final String COMPANY_NUMBER = "00006400";
    public static final int NUM_GRANTED_EXTENSION_REQS = 1;
    public static final LocalDateTime CREATED_ON = LocalDateTime.of(
        2018, 10, 10, 00,00,00);
    public static final CreatedBy CREATED_BY = createdBy();
    public static final LocalDate ACCOUNTING_PERIOD_START_ON = LocalDate.of(
        2018, 12, 12);
    public static final LocalDate ACCOUNTING_PERIOD_END_ON = LocalDate.of(
        2019, 12, 12);
    public static final Links LINKS = links();
    public static final Status STATUS_OPEN = Status.OPEN;
    public static final String BASE_URL = "/company/00006400/extensions/requests/";
    public static final String USER_ID = "userID";
    public static final String EMAIL = "email";
    public static final String FORENAME = "forename";
    public static final String SURNAME = "surname";
    public static final LocalDate REASON_START_ON = LocalDate.of(
        2018, 12, 12);
    public static final LocalDate REASON_END_ON = LocalDate.of(
        2019, 12, 12);
    public static final String REASON = "illness";
    public static final String REASON_INFORMATION = "string";
    public static final String TESTURI = "testuri";
    public static final String REASON_ID = "abc";
    public static final String ATTACHMENT_SELF_LINK = "/some/link/to/somewhere";
    public static final String ATTACHMENT_ID = "abdkskksd";
    public static final String ATTACHMENT_NAME = "certificate.pdf";
    public static final String ORIGINAL_FILE_NAME = "original.png";

    public static CreatedBy createdBy() {
        CreatedBy createdBy = new CreatedBy();
        createdBy.setId(USER_ID);
        createdBy.setForename(FORENAME);
        createdBy.setSurname(SURNAME);
        createdBy.setEmail(EMAIL);

        return createdBy;
    }

    private static Links links() {
        String linkToSelf = BASE_URL + REQUEST_ID;
        Links links = new Links();
        links.setLink(ExtensionsLinkKeys.SELF, linkToSelf);
        return links;
    }


    public static ExtensionRequestFullDTO dummyRequestDTO() {
        ExtensionRequestFullDTO requestDTO = new ExtensionRequestFullDTO();

        requestDTO.setEtag(ETAG);
        requestDTO.setId(REQUEST_ID);
        requestDTO.setCompanyNumber(COMPANY_NUMBER);
        requestDTO.setNumGrantedExtensionReqs(NUM_GRANTED_EXTENSION_REQS);
        requestDTO.setCreatedOn(CREATED_ON);
        requestDTO.setCreatedBy(CREATED_BY);
        requestDTO.setAccountingPeriodStartOn(ACCOUNTING_PERIOD_START_ON);
        requestDTO.setAccountingPeriodEndOn(ACCOUNTING_PERIOD_END_ON);
        requestDTO.setLinks(LINKS);
        requestDTO.setStatus(STATUS_OPEN);

        return requestDTO;
    }

    public static ExtensionRequestFullEntity dummyRequestEntity() {
        ExtensionRequestFullEntity requestEntity = new ExtensionRequestFullEntity();

        requestEntity.setEtag(ETAG);
        requestEntity.setId(REQUEST_ID);
        requestEntity.setCompanyNumber(COMPANY_NUMBER);
        requestEntity.setNumGrantedExtensionReqs(NUM_GRANTED_EXTENSION_REQS);
        requestEntity.setCreatedOn(CREATED_ON);
        requestEntity.setCreatedBy(CREATED_BY);
        requestEntity.setAccountingPeriodStartOn(ACCOUNTING_PERIOD_START_ON);
        requestEntity.setAccountingPeriodEndOn(ACCOUNTING_PERIOD_END_ON);
        requestEntity.setLinks(LINKS);
        requestEntity.setStatus(STATUS_OPEN);

        return requestEntity;
    }

    public static ExtensionCreateRequest dummyCreateRequestEntity() {
        ExtensionCreateRequest extensionCreateRequest = new ExtensionCreateRequest();
        extensionCreateRequest.setAccountingPeriodStartOn(ACCOUNTING_PERIOD_START_ON);
        extensionCreateRequest.setAccountingPeriodEndOn(ACCOUNTING_PERIOD_END_ON);
        return extensionCreateRequest;
    }

    public static ExtensionReasonDTO dummyReasonDTO() {
        ExtensionReasonDTO extensionReasonDTO = new ExtensionReasonDTO();
        extensionReasonDTO.setEtag(ETAG);
        extensionReasonDTO.setId(REASON_ID);
        extensionReasonDTO.setReasonInformation(REASON_INFORMATION);
        extensionReasonDTO.setStartOn(REASON_START_ON);
        extensionReasonDTO.setEndOn(REASON_END_ON);
        extensionReasonDTO.setReason(REASON);
        return extensionReasonDTO;
    }

    public static ExtensionReasonEntity dummyReasonEntity() {
        ExtensionReasonEntity extensionReasonEntity = new ExtensionReasonEntity();
        extensionReasonEntity.setEtag(ETAG);
        extensionReasonEntity.setId(REASON_ID);
        extensionReasonEntity.setReasonInformation(REASON_INFORMATION);
        extensionReasonEntity.setStartOn(REASON_START_ON);
        extensionReasonEntity.setEndOn(REASON_END_ON);
        extensionReasonEntity.setReason(REASON);
        extensionReasonEntity.setReasonStatus(ReasonStatus.DRAFT);

        return extensionReasonEntity;
    }

    public static ExtensionCreateReason dummyCreateReason() {
        ExtensionCreateReason reason = new ExtensionCreateReason();
        reason.setReasonInformation(REASON_INFORMATION);
        reason.setStartOn(REASON_START_ON);
        reason.setEndOn(REASON_END_ON);
        reason.setReason(REASON);
        return reason;
    }

    public static Attachment dummyAttachment() {
        Attachment attachment = new Attachment();
        attachment.setId(ATTACHMENT_ID);
        attachment.setId(ATTACHMENT_NAME);
        Links attachmentLinks = new Links();
        attachmentLinks.setLink(ExtensionsLinkKeys.SELF, ATTACHMENT_SELF_LINK);
        attachment.setLinks(attachmentLinks);
        return attachment;
    }

    public static MultipartFile mockMultipartFile() throws IOException {
        String fileName = "testMultipart.txt";
        Resource rsc = new ClassPathResource("input/testMultipart.txt");
        return new MockMultipartFile(fileName,
            ORIGINAL_FILE_NAME, "text/plain", Files.readAllBytes(rsc.getFile().toPath()));
    }

    public static FileTransferApiClientResponse dummyDownloadResponse() {
        FileTransferApiClientResponse dummyDownloadResponse = new FileTransferApiClientResponse();
        dummyDownloadResponse.setHttpStatus(HttpStatus.OK);
        return dummyDownloadResponse;
    }
}
