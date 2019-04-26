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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RequestRepositoryIntegrationTest {

    private static final String STATIC_REQUEST_ID = "5cc2edc4b2804e4514a4d25e";

    @Autowired
    private ExtensionRequestsRepository requestsRepository;

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
        ExtensionRequestFullEntity expectedEntity = dummyRequest();
        requestsRepository.insert(expectedEntity);

        String reasonId = UUID.randomUUID().toString();
        ExtensionReasonEntity expectedReason = dummyReason(reasonId);
        expectedEntity.setReasons(Arrays.asList(expectedReason));
        ExtensionRequestFullEntity actualEntity = requestsRepository.save(expectedEntity);

        assertEquals(1, actualEntity.getReasons().size());
        ExtensionReasonEntity actualReason = actualEntity.getReasons()
            .stream()
            .findAny()
            .orElseThrow(() -> new Exception("Reason expected in request object"));

        assertEquals("text", actualReason.getAdditionalText());
        assertEquals(reasonId, actualReason.getId());
        assertEquals("illness", actualReason.getReason());

//        Links links = new Links();
//        links.setLink(() -> "self", String.format("url/%s", reasonId));
//
//        assertEquals(links, actualReason.getLinks());
    }

    @Test
    public void canSaveAttachmentToDB() {
        ExtensionRequestFullEntity expectedEntity = dummyRequest();
        String reasonId = UUID.randomUUID().toString();
        expectedEntity.setReasons(Arrays.asList(dummyReason(reasonId)));
        ExtensionRequestFullEntity insert = requestsRepository.insert(expectedEntity);

        assertFalse(insert.getReasons().isEmpty());

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

        insert.getReasons()
            .stream()
            .forEach(reason -> {
                reason.setAttachments(Arrays.asList(expectedAttachment));
            });

        ExtensionRequestFullEntity actual = requestsRepository.save(insert);

        assertFalse(actual.getReasons().get(0).getAttachments().isEmpty());

        Attachment attachment = actual.getReasons().get(0).getAttachments().get(0);

        assertEquals(expectedAttachment.getName(), attachment.getName());
        assertEquals(attachmentId, attachment.getId());
        assertEquals(expectedAttachment.getContentType(), attachment.getContentType());
        assertEquals(expectedAttachment.getSize(), attachment.getSize());
        assertEquals(links, attachment.getLinks());
    }

    @Test
    public void canGetRequestFromDB() {
        Optional<ExtensionRequestFullEntity> dummyData = requestsRepository.findById(STATIC_REQUEST_ID);

        assertTrue(dummyData.isPresent());

        ExtensionRequestFullEntity actualEntity = dummyData.get();

        assertNotNull(actualEntity.getId());
        assertFalse(actualEntity.getReasons().isEmpty());
        assertTrue(actualEntity.getReasons()
            .stream()
            .map(ExtensionReasonEntity::getAttachments)
            .flatMap(List::stream)
            .findAny()
            .isPresent());
        assertEquals(Status.OPEN, actualEntity.getStatus());

        CreatedBy createdBy = new CreatedBy();
        createdBy.setId("Y2VkZWVlMzhlZWFjY2M4MzQ3MT");
        createdBy.setEmail("demo@ch.gov.uk");
        assertEquals(createdBy, actualEntity.getCreatedBy());
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
