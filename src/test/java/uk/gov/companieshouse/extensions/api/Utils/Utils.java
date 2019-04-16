package uk.gov.companieshouse.extensions.api.Utils;

import uk.gov.companieshouse.extensions.api.requests.CreatedBy;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullDTO;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.Status;
import uk.gov.companieshouse.service.links.Links;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Utils {
    public static final String ETAG = "etag";
    public static final String ID = "id";
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

    private static CreatedBy createdBy() {
        CreatedBy createdBy = new CreatedBy();
        createdBy.setId(USER_ID);
        createdBy.setForename(FORENAME);
        createdBy.setSurname(SURNAME);
        createdBy.setEmail(EMAIL);

        return createdBy;
    }

    private static Links links() {
        String linkToSelf = BASE_URL + ID;
        Links links = new Links();
        links.setLink(() ->  "self", linkToSelf);
        return links;
    }


    public static ExtensionRequestFullDTO dummyRequestDTO() {
        ExtensionRequestFullDTO requestDTO = new ExtensionRequestFullDTO();

        requestDTO.setEtag(ETAG);
        requestDTO.setId(ID);
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
        requestEntity.setId(ID);
        requestEntity.setCreatedOn(CREATED_ON);
        requestEntity.setCreatedBy(CREATED_BY);
        requestEntity.setAccountingPeriodStartOn(ACCOUNTING_PERIOD_START_ON);
        requestEntity.setAccountingPeriodEndOn(ACCOUNTING_PERIOD_END_ON);
        requestEntity.setLinks(LINKS);
        requestEntity.setStatus(STATUS_OPEN);

        return requestEntity;
    }
}
