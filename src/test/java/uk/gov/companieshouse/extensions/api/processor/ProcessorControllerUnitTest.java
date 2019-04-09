package uk.gov.companieshouse.extensions.api.processor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProcessorControllerUnitTest {

    private ProcessorController processorController = new ProcessorController();

    @Test
    public void openStatusReturned() {
        String status = processorController.updateExtensionRequestStatus("12344");
        assertEquals("OPEN", status);
    }
}
