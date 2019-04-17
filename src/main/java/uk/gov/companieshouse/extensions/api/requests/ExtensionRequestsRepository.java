package uk.gov.companieshouse.extensions.api.requests;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExtensionRequestsRepository extends MongoRepository<ExtensionRequestFullEntity,
    String> {
}
