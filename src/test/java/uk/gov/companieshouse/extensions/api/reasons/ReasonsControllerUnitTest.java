package uk.gov.companieshouse.extensions.api.reasons;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ReasonsControllerUnitTest {

    private ReasonsController reasonsController = new ReasonsController();

    @Test
    public void AddReasonToRequest() {
        String response = reasonsController.addReasonToRequest(dummyCreateReason(), "1234");
        assertEquals("ExtensionReason added: Extension create reason illness Additional text: string  " +
            "Date start: 2019-02-15  Date end: 2019-02-15", response);
    }

    @Test
    public void deleteReasonPlaceholderTest() {
        boolean response = reasonsController.deleteReasonFromRequest("234", "234");
        assertFalse(response);
    }

    @Test
    public void updateReasonPlaceholderTest() {
        String response = reasonsController.updateReasonOnRequest(dummyCreateReason(), "1234", "");
        assertEquals("ExtensionReason updated: Extension create reason illness Additional text: string  " +
            "Date start: 2019-02-15  Date end: 2019-02-15", response);
    }

    public static ExtensionCreateReason dummyCreateReason() {
        ExtensionCreateReason reason = new ExtensionCreateReason();
        reason.setAdditionalText("string");
        reason.setEndOn("2019-02-15");
        reason.setStartOn("2019-02-15");
        reason.setReason("illness");
        return reason;
    }
}
