package uk.gov.companieshouse.extensions.api.requests;

import org.junit.Test;
import uk.gov.companieshouse.extensions.api.Utils.Utils;

import static org.junit.Assert.assertEquals;

public class RequestMapperTest {

    private ExtensionRequestFullEntity dummyEntity = Utils.dummyRequestEntity();
    private CreatedBy createdBy = dummyEntity.getCreatedBy();

    @Test
    public void canMapEntityToDTO() {
        ExtensionRequestMapper extensionRequestMapper = new ExtensionRequestMapper();

        ExtensionRequestFullDTO dto = extensionRequestMapper.entityToDTO(dummyEntity);

        assertEquals(dummyEntity.getEtag(), dto.getEtag());
        assertEquals(dummyEntity.getId(), dto.getId());
        assertEquals(dummyEntity.getCreatedOn(), dto.getCreatedOn());
        assertEquals(dummyEntity.getLinks(), dto.getLinks());
        assertEquals(dummyEntity.getAccountingPeriodStartOn(), dto.getAccountingPeriodStartOn());
        assertEquals(dummyEntity.getAccountingPeriodEndOn(), dto.getAccountingPeriodEndOn());
        assertEquals(dummyEntity.getStatus(), dto.getStatus());
        assertEquals(createdBy.getId(), dto.getCreatedBy().getId());
        assertEquals(createdBy.getEmail(), dto.getCreatedBy().getEmail());
        assertEquals(createdBy.getForename(), dto.getCreatedBy().getForename());
        assertEquals(createdBy.getSurname(), dto.getCreatedBy().getSurname());
    }
}
