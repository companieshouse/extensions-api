package uk.gov.companieshouse.extensions.api.reasons;

import java.time.LocalDate;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;
import uk.gov.companieshouse.service.links.Links;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("Unit")
public class ExtensionReasonEntityBuilderUnitTest {


  @Test
  public void willThrowUnsupportOperationExceptionIfLinksBeforeId() {
    ExtensionReasonEntityBuilder builder = new ExtensionReasonEntityBuilder();
    UnsupportedOperationException thrown = assertThrows(UnsupportedOperationException.class, () ->
       builder.withLinks("requestURI")
         .withId("id"));
      assertEquals("Links cannot be set before ID", thrown.getMessage());
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
    assertEquals("123", entity.getId());
    assertEquals(expectedLinks, entity.getLinks());
    assertEquals("reason", entity.getReason());
    assertEquals(LocalDate.of(2018, 01, 01), entity.getEndOn());
    assertEquals(LocalDate.of(2017, 12, 12), entity.getStartOn());
    assertEquals(ReasonStatus.DRAFT, entity.getReasonStatus());
  }
}
