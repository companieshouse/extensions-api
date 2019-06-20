package uk.gov.companieshouse.extensions.api.contract;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactFolder;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import au.com.dius.pact.provider.spring.SpringRestPactRunner;
import au.com.dius.pact.provider.spring.target.SpringBootHttpTarget;
import uk.gov.companieshouse.extensions.api.groups.ContractProvider;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;
import uk.gov.companieshouse.extensions.api.requests.Status;

@Category(ContractProvider.class)
@RunWith(SpringRestPactRunner.class)
@Provider("extensions-api")
@PactFolder("pacts")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContractProviderIntegrationTest {

    @TestTarget
    public final Target target = new SpringBootHttpTarget();

    @Autowired
    private ExtensionRequestsRepository repository;

    /**
     * Verify that the test database contains an entry with supplied id and company number.
     * The status of which must be OPEN. this can be found in the mongoTestData.json resource
     * @throws Exception
     */
    @State("I have a valid OPEN request for 00006400 with requestId aaaaaaaaaaaaaaaaaaaaaaa4")
    public void toPatchState() throws Exception {
        repository.findById("aaaaaaaaaaaaaaaaaaaaaaa4")
            .map(ExtensionRequestFullEntity::getStatus)
            .filter(Status.OPEN::equals)
            .orElseThrow(() -> new Exception("Database is not in the right state for test: " +
                "Expecting request with ID aaaaaaaaaaaaaaaaaaaaaaa4 with a status of OPEN"));
    }
}