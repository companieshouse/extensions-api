package uk.gov.companieshouse.extensions.api.reasons;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import uk.gov.companieshouse.extensions.api.groups.Unit;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;
import uk.gov.companieshouse.service.links.Links;

@Category(Unit.class)
public class PatchReasonMapperUnitTest {

    @Test
    public void canMergeAPatchEntityIntoADatabaseEntity() {
        ExtensionCreateReason patchEntity = new ExtensionCreateReason();
        patchEntity.setAdditionalText("replacement text");
        patchEntity.setStartOn(LocalDate.of(2018,2,2));

        ExtensionReasonEntity dbEntity = new ExtensionReasonEntity();
        dbEntity.setId("12345");
        dbEntity.setAdditionalText("old text");
        dbEntity.setEndOn(LocalDate.of(2018,1,1));
        Links links = new Links();
        links.setLink(ExtensionsLinkKeys.SELF, "something");
        dbEntity.setLinks(links);

        ExtensionReasonEntity mappedEntity = PatchReasonMapper.INSTANCE
            .patchEntity(patchEntity, dbEntity);

        assertEquals("replacement text", mappedEntity.getAdditionalText());
        assertEquals("12345", mappedEntity.getId());
        assertEquals(LocalDate.of(2018,1,1), mappedEntity.getEndOn());
        assertEquals(links, mappedEntity.getLinks());
        assertEquals(LocalDate.of(2018,2,2), mappedEntity.getStartOn());
    }
}
