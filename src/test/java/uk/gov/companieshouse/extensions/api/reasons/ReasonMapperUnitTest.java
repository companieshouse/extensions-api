package uk.gov.companieshouse.extensions.api.reasons;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyReasonEntity;

public class ReasonMapperUnitTest {


    @Test
    public void canMapEntityToDTO() {
        ExtensionReasonMapper extensionReasonMapper = new ExtensionReasonMapper();
        ExtensionReasonEntity extensionReasonEntity = dummyReasonEntity();
        ExtensionReasonDTO extensionReasonDTO = extensionReasonMapper.entityToDTO(extensionReasonEntity);

        assertNotNull(extensionReasonDTO);
        assertEquals(extensionReasonEntity.getAdditionalText(), extensionReasonDTO.getAdditionalText());
        assertEquals(extensionReasonEntity.getStartOn(), extensionReasonDTO.getStartOn());
        assertEquals(extensionReasonEntity.getEndOn(), extensionReasonDTO.getEndOn());
        assertEquals(extensionReasonEntity.getReason(), extensionReasonDTO.getReason());
    }
}
