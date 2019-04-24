package uk.gov.companieshouse.extensions.api.reasons;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyCreateReason;

@RunWith(MockitoJUnitRunner.class)
public class ReasonServiceUnitTest {

    @InjectMocks
    private ReasonsService reasonService;

    @Test
    public void testInsertExtensionRequest() {
        ExtensionCreateReason dummyCreateReason = dummyCreateReason();
        ExtensionReason extensionReason =
            reasonService.insertExtensionsReason(dummyCreateReason);
        //TODO ensure test still passes when work is completed for lfa-610 to add reason to databse

        assertNotNull(extensionReason);
        assertEquals(dummyCreateReason.getAdditionalText(), extensionReason.getAdditionalText());
        assertEquals(dummyCreateReason.getStartOn(), extensionReason.getStartOn());
        assertEquals(dummyCreateReason.getEndOn(), extensionReason.getEndOn());
        assertEquals(dummyCreateReason.getReason(), extensionReason.getReason());

    }
}
