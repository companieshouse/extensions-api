package uk.gov.companieshouse.extensions.api.requests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;
import uk.gov.companieshouse.service.links.Links;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.REQUEST_ID;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.TESTURI;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyReasonEntity;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyRequestEntity;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
public class RequestMapperUnitTest {


    @Test
    public void canMapEntityToDTO() {
        ExtensionRequestMapper extensionRequestMapper = new ExtensionRequestMapper();
        ExtensionRequestFullEntity dummyRequest = dummyRequestEntity();
        CreatedBy createdBy = dummyRequest.getCreatedBy();
        ExtensionReasonEntity dummyReason = dummyReasonEntity();
        dummyRequest.addReason(dummyReason);
        String linkToSelf = TESTURI + "/" + REQUEST_ID;
        Links links = new Links();
        links.setLink(ExtensionsLinkKeys.SELF, linkToSelf);
        dummyReason.setLinks(links);

        ExtensionRequestFullDTO dto = extensionRequestMapper.entityToDTO(dummyRequest);
        assertNotEquals(dto.getReasons().size(), 0);
        Links reasonLinks = dto.getReasons().get(0);
        String linkValue = reasonLinks.getLink(ExtensionsLinkKeys.SELF);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(dummyRequest.getEtag(), dto.getEtag());
        Assertions.assertEquals(dummyRequest.getId(), dto.getId());
        Assertions.assertEquals(dummyRequest.getCreatedOn(), dto.getCreatedOn());
        Assertions.assertEquals(dummyRequest.getLinks(), dto.getLinks());
        Assertions.assertEquals(dummyRequest.getAccountingPeriodStartOn(), dto.getAccountingPeriodStartOn());
        Assertions.assertEquals(dummyRequest.getAccountingPeriodEndOn(), dto.getAccountingPeriodEndOn());
        Assertions.assertEquals(dummyRequest.getStatus(), dto.getStatus());
        Assertions.assertNotNull(reasonLinks);
        Assertions.assertEquals(linkToSelf, linkValue);
        Assertions.assertEquals(createdBy.getId(), dto.getCreatedBy().getId());
        Assertions.assertEquals(createdBy.getEmail(), dto.getCreatedBy().getEmail());
        Assertions.assertEquals(createdBy.getForename(), dto.getCreatedBy().getForename());
        Assertions.assertEquals(createdBy.getSurname(), dto.getCreatedBy().getSurname());
    }
}
