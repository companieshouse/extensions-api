package uk.gov.companieshouse.extensions.api.requests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.companieshouse.extensions.api.Utils.Utils;
import uk.gov.companieshouse.extensions.api.attachments.Attachment;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;
import uk.gov.companieshouse.service.links.Links;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("IntegrationTest")
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"EXTENSIONS_API_MONGODB_URL=mongodb://mongo-db1-toro1.development.aws.internal:27017", "server.port=8093",
    "api.endpoint.extensions=/company/{companyNumber}/extensions/requests",
    "spring.data.mongodb.uri=mongodb://mongo-db1-toro1.development.aws.internal:27017/extension_requests",
    "FILE_TRANSFER_API_URL=http://localhost:8081/",
    "FILE_TRANSFER_API_KEY=12345",
    "MONGO_CONNECTION_POOL_MIN_SIZE=0",
    "MONGO_CONNECTION_MAX_IDLE_TIME=0",
    "MONGO_CONNECTION_MAX_LIFE_TIME=0",
    "spring.servlet.multipart.max-file-size=100",
    "spring.servlet.multipart.max-request-size=200"})
@SpringBootTest
public class RequestRepositoryIntegrationTest {

    private static final String REQUEST_1 = "aaaaaaaaaaaaaaaaaaaaaaa1";
    private static final String REQUEST_2 = "aaaaaaaaaaaaaaaaaaaaaaa2";
    private static final String REQUEST_3 = "aaaaaaaaaaaaaaaaaaaaaaa3";

    @Autowired
    private ExtensionRequestsRepository requestsRepository;

    @Test
    public void canGetRequestFromDB() {
        Optional<ExtensionRequestFullEntity> dummyData = requestsRepository.findById(REQUEST_1);

        Assertions.assertTrue(dummyData.isPresent());

        ExtensionRequestFullEntity actualEntity = dummyData.get();

        Assertions.assertEquals(REQUEST_1, actualEntity.getId());
        Assertions.assertEquals(Status.OPEN, actualEntity.getStatus());
        Assertions.assertEquals(LocalDateTime.of(2019, 1, 2, 11, 38, 44), actualEntity.getCreatedOn());

        CreatedBy createdBy = new CreatedBy();
        createdBy.setId("Y2VkZWVlMzhlZWFjY2M4MzQ3MT");
        createdBy.setEmail("demo@ch.gov.uk");
        Assertions.assertEquals(createdBy, actualEntity.getCreatedBy());
    }

    @Test
    public void canSaveRequestToDB() {
        ExtensionRequestFullEntity expectedEntity = Utils.dummyRequestEntity();
        expectedEntity.setId(UUID.randomUUID().toString());

        ExtensionRequestFullEntity savedEntity = requestsRepository.insert(expectedEntity);

        assertNotNull(savedEntity.getId());
        assertTrue(savedEntity.getReasons().isEmpty());
        Assertions.assertEquals(LocalDate.of(2019, 12, 12), savedEntity.getAccountingPeriodEndOn());
        Assertions.assertEquals(LocalDate.of(2018, 12, 12), savedEntity.getAccountingPeriodStartOn());
        Assertions.assertEquals("etag", savedEntity.getEtag());
        Assertions.assertEquals(Status.OPEN, savedEntity.getStatus());
        Assertions.assertEquals(expectedEntity.getCreatedBy(), savedEntity.getCreatedBy());
    }

    @Test
    public void canUpdateRequestToSaveReason() throws Exception {
        ExtensionRequestFullEntity expectedEntity =
            requestsRepository.findById(REQUEST_2)
                .orElseThrow(() -> new Exception("Request not found in DB"));

        ExtensionReasonEntity expectedReason = Utils.dummyReasonEntity();
        expectedEntity.setReasons(Arrays.asList(expectedReason));
        ExtensionRequestFullEntity savedEntity = requestsRepository.save(expectedEntity);

        assertEquals(1, savedEntity.getReasons().size());
        ExtensionReasonEntity actualReason = savedEntity.getReasons()
            .stream()
            .findAny()
            .orElseThrow(() -> new Exception("Reason expected in request object"));

        assertEquals("string", actualReason.getReasonInformation());
        assertNotNull(actualReason.getId());
        assertTrue(actualReason.getAttachments().isEmpty());
        assertEquals("illness", actualReason.getReason());
    }

    @Test
    public void canSaveAttachmentToReason() throws Exception {
        ExtensionRequestFullEntity entity =
            requestsRepository.findById(REQUEST_3)
                .orElseThrow(() -> new Exception("Request not found in DB"));

        Attachment expectedAttachment = new Attachment();
        String attachmentId = UUID.randomUUID().toString();
        expectedAttachment.setId(attachmentId);
        expectedAttachment.setName("file");
        expectedAttachment.setContentType("content");
        expectedAttachment.setSize(1L);

        Links links = new Links();
        links.setLink(ExtensionsLinkKeys.SELF, "selfLink");
        links.setLink(ExtensionsLinkKeys.DOWNLOAD, "downloadLink");
        expectedAttachment.setLinks(links);

        entity.getReasons()
            .forEach(reason -> reason.setAttachments(Arrays.asList(expectedAttachment)));

        ExtensionRequestFullEntity savedEntity = requestsRepository.save(entity);

        assertFalse(savedEntity.getReasons().get(0).getAttachments().isEmpty());

        Attachment attachment = savedEntity.getReasons().get(0).getAttachments().get(0);

        assertEquals(expectedAttachment.getName(), attachment.getName());
        assertEquals(attachmentId, attachment.getId());
        assertEquals(expectedAttachment.getContentType(), attachment.getContentType());
        assertEquals(expectedAttachment.getSize(), attachment.getSize());
        assertEquals(links, attachment.getLinks());
    }

    @Test
    public void canGetMultipleRequestsFromDB() throws Exception {
        ExtensionRequestFullEntity dummyEntity = requestsRepository.findById(REQUEST_1)
            .orElseThrow(() -> new Exception("Request not found in DB"));

        List<ExtensionRequestFullEntity> entityList = requestsRepository
            .findAllByCompanyNumber("00008787", Sort.by("_id"));

        assertEquals(2, entityList.size());
        assertEquals(dummyEntity.getId(), entityList.get(0).getId());
        assertEquals(dummyEntity.getCreatedOn(), entityList.get(0).getCreatedOn());
        assertEquals(dummyEntity.getCreatedBy(), entityList.get(0).getCreatedBy());
        assertEquals(dummyEntity.getCreatedBy(), entityList.get(0).getCreatedBy());
        assertEquals(dummyEntity.getLinks(), entityList.get(0).getLinks());
        assertEquals(dummyEntity.getStatus(), entityList.get(0).getStatus());
        assertEquals(dummyEntity.getClass(), entityList.get(0).getClass());
        assertEquals(dummyEntity.getReasons(), entityList.get(0).getReasons());
    }
}
