package uk.gov.companieshouse.extensions.api.requests;

import org.junit.Test;

import uk.gov.companieshouse.extensions.api.Utils.Utils;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;
import uk.gov.companieshouse.service.links.LinkKey;
import uk.gov.companieshouse.service.links.Links;

import java.util.Map;

import static org.junit.Assert.*;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.*;

public class RequestMapperUnitTest {


    @Test
    public void canMapEntityToDTO() {
        ExtensionRequestMapper extensionRequestMapper = new ExtensionRequestMapper();
        ExtensionRequestFullEntity dummyRequest = dummyRequestEntity();
        CreatedBy createdBy = dummyRequest.getCreatedBy();
        ExtensionReasonEntity dummyReason = dummyReasonEntity();
        dummyRequest.addReason(dummyReason);
        String linkToSelf = TESTURI  + "/" + REQUEST_ID;
        Links links = new Links();
        links.setLink(() ->  "self", linkToSelf);
        dummyReason.setLinks(links);

        ExtensionRequestFullDTO dto = extensionRequestMapper.entityToDTO(dummyRequest);
        assertNotEquals(dto.getReasons().size(), 0);
        Links reasonLinks = dto.getReasons().get(0);
        String linkValue = reasonLinks.getLinks().get("self");

        assertNotNull(dto);
        assertEquals(dummyRequest.getEtag(), dto.getEtag());
        assertEquals(dummyRequest.getId(), dto.getId());
        assertEquals(dummyRequest.getCreatedOn(), dto.getCreatedOn());
        assertEquals(dummyRequest.getLinks(), dto.getLinks());
        assertEquals(dummyRequest.getAccountingPeriodStartOn(), dto.getAccountingPeriodStartOn());
        assertEquals(dummyRequest.getAccountingPeriodEndOn(), dto.getAccountingPeriodEndOn());
        assertEquals(dummyRequest.getStatus(), dto.getStatus());
        assertNotNull(reasonLinks);
        assertEquals(linkToSelf, linkValue);
        assertEquals(createdBy.getId(), dto.getCreatedBy().getId());
        assertEquals(createdBy.getEmail(), dto.getCreatedBy().getEmail());
        assertEquals(createdBy.getForename(), dto.getCreatedBy().getForename());
        assertEquals(createdBy.getSurname(), dto.getCreatedBy().getSurname());
    }
}
