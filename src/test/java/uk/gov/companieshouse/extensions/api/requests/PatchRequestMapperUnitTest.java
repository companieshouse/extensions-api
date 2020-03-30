package uk.gov.companieshouse.extensions.api.requests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import uk.gov.companieshouse.extensions.api.groups.Unit;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;
import uk.gov.companieshouse.service.links.Links;

@Category(Unit.class)
public class PatchRequestMapperUnitTest {

    private ExtensionRequestFullEntity dbEntity;
    private Links links;

    @Before
    public void setup() {
        dbEntity = new ExtensionRequestFullEntity();
        dbEntity.setId("12345");
        dbEntity.setAccountingPeriodEndOn(LocalDate.of(2018,1,1));

        links = new Links();
        links.setLink(ExtensionsLinkKeys.SELF, "something");
        dbEntity.setLinks(links);

        ExtensionReasonEntity reason = new ExtensionReasonEntity();
        reason.setId("reason1");
        dbEntity.setReasons(Arrays.asList(reason));
    }

    @Test
    public void canMergeAPatchEntityIntoADatabaseEntity_Submitted() {
        RequestStatus patchEntity = new RequestStatus();
        patchEntity.setStatus(Status.SUBMITTED);
        patchEntity.setIsAutoAccepted(true);

        ExtensionRequestFullEntity mappedEntity = PatchRequestMapper.INSTANCE
            .patchEntity(patchEntity, dbEntity);

        assertEquals("12345", mappedEntity.getId());
        assertEquals(LocalDate.of(2018,1,1), mappedEntity.getAccountingPeriodEndOn());
        assertEquals(links, mappedEntity.getLinks());
        assertEquals(Status.SUBMITTED, dbEntity.getStatus());
        assertEquals("reason1", dbEntity.getReasons().get(0).getId());
        assertTrue(dbEntity.getIsAutoAccepted());
    }

    @Test
    public void canMergeAPatchEntityIntoADatabaseEntity_Processing() {
        RequestStatus patchEntity = new RequestStatus();
        patchEntity.setStatus(Status.PROCESSING);
        patchEntity.setIsAutoAccepted(true);

        ExtensionRequestFullEntity mappedEntity = PatchRequestMapper.INSTANCE
            .patchEntity(patchEntity, dbEntity);

        assertEquals("12345", mappedEntity.getId());
        assertEquals(LocalDate.of(2018,1,1), mappedEntity.getAccountingPeriodEndOn());
        assertEquals(links, mappedEntity.getLinks());
        assertEquals(Status.PROCESSING, dbEntity.getStatus());
        assertEquals("reason1", dbEntity.getReasons().get(0).getId());
        assertTrue(dbEntity.getIsAutoAccepted());
    }

    @Test
    public void canMergeAPatchEntityIntoADatabaseEntity_Chips_Sent() {
        RequestStatus patchEntity = new RequestStatus();
        patchEntity.setStatus(Status.CHIPS_SENT);
        patchEntity.setIsAutoAccepted(true);

        ExtensionRequestFullEntity mappedEntity = PatchRequestMapper.INSTANCE
            .patchEntity(patchEntity, dbEntity);

        assertEquals("12345", mappedEntity.getId());
        assertEquals(LocalDate.of(2018,1,1), mappedEntity.getAccountingPeriodEndOn());
        assertEquals(links, mappedEntity.getLinks());
        assertEquals(Status.CHIPS_SENT, dbEntity.getStatus());
        assertEquals("reason1", dbEntity.getReasons().get(0).getId());
        assertTrue(dbEntity.getIsAutoAccepted());
    }
}
