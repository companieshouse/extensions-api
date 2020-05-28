package uk.gov.companieshouse.extensions.api.requests;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;
import uk.gov.companieshouse.service.links.Links;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("Unit")
public class PatchRequestMapperUnitTest {

    @Test
    public void canMergeAPatchEntityIntoADatabaseEntity() {
        RequestStatus patchEntity = new RequestStatus();
        patchEntity.setStatus(Status.SUBMITTED);
        patchEntity.setIsAutoAccepted(true);

        ExtensionRequestFullEntity dbEntity = new ExtensionRequestFullEntity();
        dbEntity.setId("12345");
        dbEntity.setAccountingPeriodEndOn(LocalDate.of(2018,1,1));
        Links links = new Links();
        links.setLink(ExtensionsLinkKeys.SELF, "something");
        dbEntity.setLinks(links);

        ExtensionReasonEntity reason = new ExtensionReasonEntity();
        reason.setId("reason1");
        dbEntity.setReasons(Arrays.asList(reason));
        ExtensionRequestFullEntity mappedEntity = PatchRequestMapper.INSTANCE
            .patchEntity(patchEntity, dbEntity);

        assertEquals("12345", mappedEntity.getId());
        assertEquals(LocalDate.of(2018,1,1), mappedEntity.getAccountingPeriodEndOn());
        assertEquals(links, mappedEntity.getLinks());
        assertEquals(Status.SUBMITTED, dbEntity.getStatus());
        assertEquals("reason1", dbEntity.getReasons().get(0).getId());
        assertTrue(dbEntity.getIsAutoAccepted());
    }
  
}
