package uk.gov.companieshouse.extensions.api.requests;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExtensionRequestsRepository extends MongoRepository<ExtensionRequestFullEntity,
    String> {

    List<ExtensionRequestFullEntity> findAllByCompanyNumber(final String companyNumber, Sort sort);

    Optional<ExtensionRequestFullEntity> findByIdAndCompanyNumber(String id, String companyNumber);
}
