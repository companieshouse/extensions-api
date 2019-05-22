package uk.gov.companieshouse.extensions.api.requests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.REQUEST_ID;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.TESTURI;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyReasonEntity;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyRequestEntity;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import uk.gov.companieshouse.extensions.api.groups.Unit;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;
import uk.gov.companieshouse.service.links.Links;

@Category(Unit.class)
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
