package uk.gov.companieshouse.extensions.api.attachments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReason;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.links.Links;

import java.util.List;
import java.util.Optional;

@Service
public class AttachmentsService {

    @Autowired
    private ExtensionRequestsRepository requestsRepository;

    public ServiceResult<AttachmentsMetadata> addAttachment(MultipartFile file,
                                                            String attachmentsUri,
                                                            String requestId, String reasonId) throws Exception {
        AttachmentsMetadata attachmentsMetadata = new AttachmentsMetadata(attachmentsUri,
            "scanned");
        attachmentsMetadata.setSize(file.getSize());
        String linkToSelf = attachmentsUri + savedEntity.getId();
        Links links = new Links();
        links.setLink(() ->  "self", linkToSelf);
        attachmentsMetadata.setLinks(links);

        Optional<ExtensionRequestFullEntity> extension = requestsRepository.findById(requestId);
        List<ExtensionReason> reasons =
            extension.map(ext -> ext.getReasons()).orElseThrow(() -> new Exception());
        reasons.stream()
               .filter(reason -> reason.getId().equals(reasonId))
               .forEachOrdered(reason -> reason.addAttachment(attachmentsMetadata));

        return ServiceResult.accepted(attachmentsMetadata);
    }
}
