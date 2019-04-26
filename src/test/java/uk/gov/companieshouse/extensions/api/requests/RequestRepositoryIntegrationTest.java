package uk.gov.companieshouse.extensions.api.requests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.companieshouse.service.links.Links;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RequestRepositoryIntegrationTest {

    @Autowired
    private ExtensionRequestsRepository requestsRepository;

    @Test
    public void canInputAndReadRequestFromDB() {
        ExtensionCreateRequest createRequest = new ExtensionCreateRequest();
        createRequest.setAccountingPeriodEndOn(LocalDate.of(2018, 2, 1));
        createRequest.setAccountingPeriodStartOn(LocalDate.of(2018, 1, 1));

        CreatedBy createdBy = new CreatedBy();
        createdBy.setEmail("emailAddress");
        createdBy.setForename("Joe");
        createdBy.setSurname("Bloggs");
        createdBy.setId("123");

        ExtensionRequestFullEntity expectedEntity =
            requestService.insertExtensionsRequest(createRequest, createdBy, "dummyUri/");

        ExtensionRequestFullEntity actualEntity =
            requestService.getExtensionsRequestById(expectedEntity.getId());

        assertTrue(actualEntity.getReasons().isEmpty());
        assertEquals(LocalDate.of(2018, 2, 1), actualEntity.getAccountingPeriodEndOn());
        assertEquals(LocalDate.of(2018, 1, 1), actualEntity.getAccountingPeriodStartOn());
        assertEquals(null, actualEntity.getEtag());
        assertEquals(expectedEntity.getId(), actualEntity.getId());

        Links links = new Links();
        links.setLink(() -> "self", "dummyUri/" + expectedEntity.getId());
        assertEquals(links, actualEntity.getLinks());
        assertEquals(Status.OPEN, actualEntity.getStatus());
        assertEquals(createdBy, actualEntity.getCreatedBy());
    }
}
