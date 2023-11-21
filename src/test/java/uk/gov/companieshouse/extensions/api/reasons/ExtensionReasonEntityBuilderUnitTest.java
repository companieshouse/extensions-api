package uk.gov.companieshouse.extensions.api.reasons;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;
import uk.gov.companieshouse.service.links.Links;

import java.time.LocalDate;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
public class ExtensionReasonEntityBuilderUnitTest {

    @Test
    public void willThrowUnsupportOperationExceptionIfLinksBeforeId() {
        ExtensionReasonEntityBuilder builder = new ExtensionReasonEntityBuilder();
        UnsupportedOperationException unsupportedOperationException = Assertions.assertThrows(UnsupportedOperationException.class, () -> builder.withLinks("requestURI")
            .withId("id"));
        Assertions.assertEquals(unsupportedOperationException.getMessage(), "Links cannot be set before ID");
    }

    @Test
    public void willCreateAReasonEntity() {
        ExtensionReasonEntity entity = new ExtensionReasonEntityBuilder()
            .withId("123")
            .withLinks("requestURI")
            .withEndOn(LocalDate.of(2018, 01, 01))
            .withStartOn(LocalDate.of(2017, 12, 12))
            .withReason("reason")
            .withReasonStatus(ReasonStatus.DRAFT)
            .build();


        Links expectedLinks = new Links();
        expectedLinks.setLink(ExtensionsLinkKeys.SELF, "requestURI/123");
        Assertions.assertEquals("123", entity.getId());
        Assertions.assertEquals(expectedLinks, entity.getLinks());
        Assertions.assertEquals("reason", entity.getReason());
        Assertions.assertEquals(LocalDate.of(2018, 01, 01), entity.getEndOn());
        Assertions.assertEquals(LocalDate.of(2017, 12, 12), entity.getStartOn());
        Assertions.assertEquals(ReasonStatus.DRAFT, entity.getReasonStatus());
    }
}
