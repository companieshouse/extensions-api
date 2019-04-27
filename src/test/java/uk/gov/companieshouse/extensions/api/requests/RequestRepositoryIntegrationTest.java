package uk.gov.companieshouse.extensions.api.requests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.companieshouse.extensions.api.attachments.Attachment;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntityBuilder;
import uk.gov.companieshouse.service.links.Links;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
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

        assertTrue(dummyData.isPresent());

        ExtensionRequestFullEntity actualEntity = dummyData.get();

        assertEquals(REQUEST_1, actualEntity.getId());
        assertEquals(Status.OPEN, actualEntity.getStatus());
        assertEquals(LocalDateTime.of(2019,1, 2, 11, 38, 44), actualEntity.getCreatedOn());

        CreatedBy createdBy = new CreatedBy();
        createdBy.setId("Y2VkZWVlMzhlZWFjY2M4MzQ3MT");
        createdBy.setEmail("demo@ch.gov.uk");
        assertEquals(createdBy, actualEntity.getCreatedBy());
    }

    @Test
    public void canSaveRequestToDB() {
        ExtensionRequestFullEntity expectedEntity = dummyRequest();

        ExtensionRequestFullEntity savedEntity = requestsRepository.insert(expectedEntity);

        assertNotNull(savedEntity.getId());
        assertTrue(savedEntity.getReasons().isEmpty());
        assertEquals(LocalDate.of(2018, 2, 1), savedEntity.getAccountingPeriodEndOn());
        assertEquals(LocalDate.of(2018, 1, 1), savedEntity.getAccountingPeriodStartOn());
        assertEquals(null, savedEntity.getEtag());
        assertEquals(Status.OPEN, savedEntity.getStatus());
        assertEquals(expectedEntity.getCreatedBy(), savedEntity.getCreatedBy());
    }

    @Test
    public void canUpdateRequestToSaveReason() throws Exception {
        ExtensionRequestFullEntity expectedEntity =
            requestsRepository.findById(REQUEST_2)
                .orElseThrow(() -> new Exception("Request not found in DB"));

        String reasonId = UUID.randomUUID().toString();
        ExtensionReasonEntity expectedReason = dummyReason(reasonId);
        expectedEntity.setReasons(Arrays.asList(expectedReason));
        ExtensionRequestFullEntity savedEntity = requestsRepository.save(expectedEntity);

        assertEquals(1, savedEntity.getReasons().size());
        ExtensionReasonEntity actualReason = savedEntity.getReasons()
            .stream()
            .findAny()
            .orElseThrow(() -> new Exception("Reason expected in request object"));

        assertEquals("text", actualReason.getAdditionalText());
        assertEquals(reasonId, actualReason.getId());
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
        links.setLink(() -> "self", "selfLink");
        links.setLink(() -> "download", "downloadLink");
        expectedAttachment.setLinks(links);

        entity.getReasons()
            .stream()
            .forEach(reason -> {
                reason.setAttachments(Arrays.asList(expectedAttachment));
            });

        ExtensionRequestFullEntity savedEntity = requestsRepository.save(entity);

        assertFalse(savedEntity.getReasons().get(0).getAttachments().isEmpty());

        Attachment attachment = savedEntity.getReasons().get(0).getAttachments().get(0);

        assertEquals(expectedAttachment.getName(), attachment.getName());
        assertEquals(attachmentId, attachment.getId());
        assertEquals(expectedAttachment.getContentType(), attachment.getContentType());
        assertEquals(expectedAttachment.getSize(), attachment.getSize());
        assertEquals(links, attachment.getLinks());
    }

    private ExtensionRequestFullEntity dummyRequest() {
        CreatedBy createdBy = new CreatedBy();
        createdBy.setEmail("emailAddress");
        createdBy.setForename("Joe");
        createdBy.setSurname("Bloggs");
        createdBy.setId("123");

        return ExtensionRequestFullEntityBuilder
            .newInstance()
            .withAccountingPeriodEndOn(LocalDate.of(2018, 2, 1))
            .withAccountingPeriodStartOn(LocalDate.of(2018, 1, 1))
            .withCreatedBy(createdBy)
            .withCreatedOn(() -> LocalDateTime.of(2019, 1, 1, 1, 1))
            .withStatus()
            .build();
    }

    private ExtensionReasonEntity dummyReason(String reasonId) {
        ExtensionReasonEntity reason = ExtensionReasonEntityBuilder
            .getInstance()
            .withAdditionalText("text")
            .withStartOn(LocalDate.of(2019, 1, 1))
            .withEndOn(LocalDate.of(2019, 2, 1))
            .withReason("illness")
            .withLinks("url")
            .build();
        reason.setId(reasonId);
        return reason;
    }
}
