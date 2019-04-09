package uk.gov.companieshouse.extensions.api.reasons;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ReasonsControllerUnitTest {

    private ReasonsController reasonsController = new ReasonsController();

    @Test
    public void AddReasonToRequest() {
        String response = reasonsController.addReasonToRequest(dummyReason(), "1234");
        assertEquals("ExtensionReason added: Extension reason illness Additional text: string  " +
            "Date start: 2019-02-15  Date end: 2019-02-15", response);
    }

    @Test
    public void deleteReasonPlaceholderTest() {
        boolean response = reasonsController.deleteReasonFromRequest("234", "234");
        assertFalse(response);
    }

    @Test
    public void updateReasonPlaceholderTest() {
        String response = reasonsController.updateReasonOnRequest(dummyReason(), "1234", "");
        assertEquals("ExtensionReason updated: Extension reason illness Additional text: string  " +
            "Date start: 2019-02-15  Date end: 2019-02-15", response);
    }

    public static ExtensionReason dummyReason() {
        ExtensionReason reason = new ExtensionReason();
        reason.setAdditionalText("string");
        reason.setDateEnd("2019-02-15");
        reason.setDateStart("2019-02-15");
        reason.setReason("illness");
        return reason;
    }
}
