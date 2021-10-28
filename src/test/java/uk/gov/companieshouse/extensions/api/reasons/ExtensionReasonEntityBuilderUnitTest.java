package uk.gov.companieshouse.extensions.api.reasons;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.ExpectedException;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;
import uk.gov.companieshouse.service.links.Links;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExtensionReasonEntityBuilderUnitTest {

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Test
  public void willThrowUnsupportOperationExceptionIfLinksBeforeId() {
    expectedException.expect(UnsupportedOperationException.class);
    expectedException.expectMessage("Links cannot be set before ID");
    ExtensionReasonEntityBuilder builder = new ExtensionReasonEntityBuilder();
    builder.withLinks("requestURI")
      .withId("id");
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
