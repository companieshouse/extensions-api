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
import uk.gov.companieshouse.extensions.api.authorization.CompanyAuthorizationInterceptor;
import uk.gov.companieshouse.extensions.api.groups.ContractProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * HOW TO RUN LOCALLY
 * ------------------
 * These have been setup to run as integration tests on the pipeline (extensions-processer-api alsoo has it's own
 * contract tests).
 *
 * The pact file are generated and uploaded to S3 on the extensions-processor-api pipeline
 * Before the tests are run on the pipeline there is a pipeline job that loads the test data into the TORO1 Mongo DB.
 * The data to be loaded is found in this project under src/test/resources/mongoTestData.json
 *
 * To run locally, you need to download the pact files from AWS S3 bucket chips-assets-dev/pacts and place inside
 * a 'pacts' folder in the root of this project.
 * The data should already be loaded into the TORO1 Mongo DB extensions_requests collection by the pipeline.
 * First time you run it 'should' work.
 *
 * The DELETE attachment test for request aaaaaaaaaaaaaaaaaaaaaa15 will only run once, then fail thereafter locally,
 * due to the test actually removing the attachment from the mongo data. To get it to run again, you have to manually
 * update the aaaaaaaaaaaaaaaaaaaaaa15 record in mongo using the json data in src/test/resources/mongoTestData.json
 * to either re-add the attachment, or just replace the whole record with the one from test data.
 *
 */
@Category(ContractProvider.class)
@RunWith(SpringRestPactRunner.class)
@Provider("extensions-api")
@PactFolder("pacts")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ContractProviderIntegrationTest {

    @MockBean
    private FileTransferApiClient fileTransferApiClient;

    @MockBean
    private CompanyAuthorizationInterceptor mockAuthInterceptor;

    @TestTarget
    public final Target target = new SpringBootHttpTarget();

    @Before
    public void setup() {
        FileTransferApiClientResponse fileTransferApiClientResponse = new FileTransferApiClientResponse();
        fileTransferApiClientResponse.setHttpStatus(HttpStatus.NO_CONTENT);
        when(fileTransferApiClient.delete(anyString())).thenReturn(fileTransferApiClientResponse);
        when(mockAuthInterceptor.preHandle(any(HttpServletRequest.class), 
            any(HttpServletResponse.class), any(Object.class)))
              .thenReturn(true);
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
