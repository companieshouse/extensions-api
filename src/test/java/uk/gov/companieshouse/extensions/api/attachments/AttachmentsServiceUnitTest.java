package uk.gov.companieshouse.extensions.api.attachments;

import org.junit.Test;
import uk.gov.companieshouse.service.ServiceResult;

import static org.junit.Assert.assertEquals;

public class AttachmentsServiceUnitTest {

    private AttachmentsService service = new AttachmentsService();

    @Test
    public void canAddAnAttachment() {
        ServiceResult<AttachmentsMetadata> result = service.addAttachment();

        AttachmentsMetadata expectedMetadata = new AttachmentsMetadata("/dummyUrl", "scanned");
        ServiceResult<AttachmentsMetadata> expectedResult =
            ServiceResult.accepted(expectedMetadata);

        assertEquals(expectedResult, result);
    }
}
