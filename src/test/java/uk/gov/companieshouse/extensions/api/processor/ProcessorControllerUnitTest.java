package uk.gov.companieshouse.extensions.api.processor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import uk.gov.companieshouse.extensions.api.groups.Unit;

@Category(Unit.class)
public class ProcessorControllerUnitTest {

    private ProcessorController processorController = new ProcessorController();

    @Test
    public void openStatusReturned() {
        String status = processorController.updateExtensionRequestStatus("12344");
        assertEquals("OPEN", status);
    }
}
