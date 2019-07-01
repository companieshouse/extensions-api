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

    @State("I have a valid OPEN request for 00006400 with requestId aaaaaaaaaaaaaaaaaaaaaaa4")
    public void toPatchState() {}

    @State("i have full request object")
    public void fullRequestObject() {}

    @State("I am expecting a post request")
    public void expectPostRequest() {}

    @State("I have extension request aaaaaaaaaaaaaaaaaaaaaa14 for company number 00006400")
    public void a14RequestForGirlsSchool() {}

    @State("I have extension request aaaaaaaaaaaaaaaaaaaaaa16 with reasonId: reason1 without reason information")
    public void a16RequestForGirlsSchool() {}

    @State("I have extension request aaaaaaaaaaaaaaaaaaaaaa13 with reasonId: reason1")
    public void a13RequestWithReason() {}

    @State("I have extension request aaaaaaaaaaaaaaaaaaaaaa12 with reasonId: reason1")
    public void a12RequestWithReason() {}
    
    @State("I have extension request aaaaaaaaaaaaaaaaaaaaaa15 with reasonId: reason1 and attachment: attachment1")
    public void a15RequestWithReasonAndAttachment() {}
}