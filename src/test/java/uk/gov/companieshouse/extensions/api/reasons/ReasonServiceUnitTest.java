package uk.gov.companieshouse.extensions.api.reasons;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.*;


@RunWith(MockitoJUnitRunner.class)
public class ReasonServiceUnitTest {

    @InjectMocks
    private ReasonsService reasonService;

    @Mock
    private ExtensionRequestsRepository extensionRequestsRepository;

    @Test
    public void testAddExtensionsReasonToRequest() {
        ExtensionCreateReason dummyCreateReason = dummyCreateReason();

        ExtensionReasonEntityBuilder extensionReasonEntityBuilder
            = ExtensionReasonEntityBuilder.getInstance();

        ExtensionReasonEntity extensionReason = extensionReasonEntityBuilder
            .withAdditionalText(dummyCreateReason.getAdditionalText())
            .withStartOn(dummyCreateReason.getStartOn())
            .withEndOn(dummyCreateReason.getEndOn())
            .withReason(dummyCreateReason.getReason())
            .build();

        assertNotNull(extensionReason);
        assertEquals(dummyCreateReason.getAdditionalText(), extensionReason.getAdditionalText());
        assertEquals(dummyCreateReason.getStartOn(), extensionReason.getStartOn());
        assertEquals(dummyCreateReason.getEndOn(), extensionReason.getEndOn());
        assertEquals(dummyCreateReason.getReason(), extensionReason.getReason());

    }
}
