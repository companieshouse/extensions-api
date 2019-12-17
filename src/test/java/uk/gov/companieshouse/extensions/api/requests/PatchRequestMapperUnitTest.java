package uk.gov.companieshouse.extensions.api.requests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import uk.gov.companieshouse.extensions.api.groups.Unit;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;
import uk.gov.companieshouse.service.links.Links;

@Category(Unit.class)
public class PatchRequestMapperUnitTest {

    @Test
    public void canMergeAPatchEntityIntoADatabaseEntity() {
        RequestStatus patchEntity = new RequestStatus();
        patchEntity.setStatus(Status.SUBMITTED);
        patchEntity.setIsAutoAccepted(true);

        ExtensionRequestFullEntity dbEntity = new ExtensionRequestFullEntity();
        dbEntity.setId("12345");
        dbEntity.setAccountingPeriodEndOn(LocalDateTime.of(2018,1,1, 0, 0, 0));
        Links links = new Links();
        links.setLink(ExtensionsLinkKeys.SELF, "something");
        dbEntity.setLinks(links);

        ExtensionReasonEntity reason = new ExtensionReasonEntity();
        reason.setId("reason1");
        dbEntity.setReasons(Arrays.asList(reason));
        ExtensionRequestFullEntity mappedEntity = PatchRequestMapper.INSTANCE
            .patchEntity(patchEntity, dbEntity);

        assertEquals("12345", mappedEntity.getId());
        assertEquals(LocalDateTime.of(2018,1,1, 0, 0, 0), mappedEntity.getAccountingPeriodEndOn());
        assertEquals(links, mappedEntity.getLinks());
        assertEquals(Status.SUBMITTED, dbEntity.getStatus());
        assertEquals("reason1", dbEntity.getReasons().get(0).getId());
        assertTrue(dbEntity.getIsAutoAccepted());
    }
  
}
