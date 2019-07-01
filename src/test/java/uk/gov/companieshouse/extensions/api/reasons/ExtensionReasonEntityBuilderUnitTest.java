package uk.gov.companieshouse.extensions.api.reasons;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

import uk.gov.companieshouse.extensions.api.groups.Unit;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;
import uk.gov.companieshouse.service.links.Links;

@Category(Unit.class)
public class ExtensionReasonEntityBuilderUnitTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void willThrowUnsupportOperationExceptionIfLinksBeforeId() {
    expectedException.expect(UnsupportedOperationException.class);
    expectedException.expectMessage("Links cannot be set before ID");
    ExtensionReasonEntityBuilder builder = new ExtensionReasonEntityBuilder();
    builder.withLinks("requestURI")
      .withId("id");
  }

  @Test
  public void willCreateAReasonEnity() {
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
