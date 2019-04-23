package uk.gov.companieshouse.extensions.api.attachments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReason;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.links.Links;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AttachmentsService {

    private ExtensionRequestsRepository requestsRepo;

    @Autowired
    public AttachmentsService(ExtensionRequestsRepository requestsRepo) {
        this.requestsRepo = requestsRepo;
    }

    public ServiceResult<AttachmentsMetadata>
            addAttachment(MultipartFile file,
                          String attachmentsUri, String requestId,
                          String reasonId) throws Exception {
        String randomUUID = UUID.randomUUID().toString();

        AttachmentsMetadata attachmentsMetadata = new AttachmentsMetadata(attachmentsUri,
            "scanned");
        attachmentsMetadata.setSize(file.getSize());
        attachmentsMetadata.setId(randomUUID);

        Optional<ExtensionRequestFullEntity> extension = requestsRepo.findById(requestId);

        List<ExtensionReasonEntity> reasons =
            extension.map(ext -> ext.getReasons()).orElseThrow(() -> new Exception("You cannot " +
                "add an attachment to a request without a reason"));
        reasons.stream()
               .filter(reason -> reason.getId().equals(reasonId))
               .forEachOrdered(reason -> reason.addAttachment(attachmentsMetadata));

        requestsRepo.save(extension.get());

        String linkToSelf = attachmentsUri + randomUUID;
        Links links = new Links();
        links.setLink(() ->  "self", linkToSelf);
        attachmentsMetadata.setLinks(links);

        return ServiceResult.accepted(attachmentsMetadata);
    }
}
