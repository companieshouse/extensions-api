package uk.gov.companieshouse.extensions.api.requests;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ExtensionRequestsRepository extends MongoRepository<ExtensionRequestFullEntity,
    String> {

    List<ExtensionRequestFullEntity> findAllByCompanyNumber(final String companyNumber, Sort sort);
}
