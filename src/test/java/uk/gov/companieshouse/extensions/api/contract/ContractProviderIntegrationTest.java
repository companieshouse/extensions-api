package uk.gov.companieshouse.extensions.api.contract;

import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactFolder;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import au.com.dius.pact.provider.spring.SpringRestPactRunner;
import au.com.dius.pact.provider.spring.target.SpringBootHttpTarget;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.extensions.api.attachments.file.FileTransferApiClient;
import uk.gov.companieshouse.extensions.api.attachments.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.extensions.api.groups.ContractProvider;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Category(ContractProvider.class)
@RunWith(SpringRestPactRunner.class)
@Provider("extensions-api")
@PactFolder("pacts")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContractProviderIntegrationTest {

    @MockBean
    private FileTransferApiClient fileTransferApiClient;

    @TestTarget
    public final Target target = new SpringBootHttpTarget();

    @Before
    public void setup() {
        FileTransferApiClientResponse fileTransferApiClientResponse = new FileTransferApiClientResponse();
        fileTransferApiClientResponse.setHttpStatus(HttpStatus.NO_CONTENT);
        when(fileTransferApiClient.delete(anyString())).thenReturn(fileTransferApiClientResponse);
    }

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
