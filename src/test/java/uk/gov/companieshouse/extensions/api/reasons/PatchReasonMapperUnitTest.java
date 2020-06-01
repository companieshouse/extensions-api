package uk.gov.companieshouse.extensions.api.reasons;

import java.time.LocalDate;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;
import uk.gov.companieshouse.service.links.Links;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
public class PatchReasonMapperUnitTest {

    @Test
    public void canMergeAPatchEntityIntoADatabaseEntity() {
        ExtensionCreateReason patchEntity = new ExtensionCreateReason();
        patchEntity.setReasonInformation("replacement text");
        patchEntity.setStartOn(LocalDate.of(2018,2,2));
        patchEntity.setReasonStatus(ReasonStatus.COMPLETED);

        ExtensionReasonEntity dbEntity = new ExtensionReasonEntity();
        dbEntity.setId("12345");
        dbEntity.setReasonInformation("old text");
        dbEntity.setEndOn(LocalDate.of(2018,1,1));
        dbEntity.setReasonStatus(ReasonStatus.DRAFT);
        Links links = new Links();
        links.setLink(ExtensionsLinkKeys.SELF, "something");
        dbEntity.setLinks(links);

        ExtensionReasonEntity mappedEntity = PatchReasonMapper.INSTANCE
            .patchEntity(patchEntity, dbEntity);

        assertEquals("replacement text", mappedEntity.getReasonInformation());
        assertEquals("12345", mappedEntity.getId());
        assertEquals(LocalDate.of(2018,1,1), mappedEntity.getEndOn());
        assertEquals(links, mappedEntity.getLinks());
        assertEquals(LocalDate.of(2018,2,2), mappedEntity.getStartOn());
        assertEquals(ReasonStatus.COMPLETED, mappedEntity.getReasonStatus());
    }
}
