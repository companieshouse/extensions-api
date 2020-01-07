package uk.gov.companieshouse.extensions.api.requests;

import com.mongodb.MongoClient;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.companieshouse.extensions.api.groups.Integration;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.Assert.assertEquals;

/**
 * Tests require actual connection to mongo db in order to run so these will be ignored
 * so that testing does not fail on concourse just because this connection is absent.
 */


@Category(Integration.class)
@RunWith(SpringRunner.class)
@SpringBootTest
public class MongoDBTest {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static final String WINTER_TEST_DOCUMENT = "winter_systime_test_123";
    public static final String SUMMER_TEST_DOCUMENT = "summer_systime_test_123";

    @Autowired
    private ExtensionRequestsRepository requestsRepository;

    private MongoClient mongoClient = new MongoClient();

    private LocalDate winterStart;
    private LocalDate winterEnd;
    private LocalDate summerStart;
    private LocalDate summerEnd;

    @Before
    public void init() {
        winterStart = LocalDate.of(2020,1,1);
        winterEnd = LocalDate.of(2020,1,6);
        summerStart = LocalDate.of(2020,7,1);
        summerEnd = LocalDate.of(2020,7,6);

        MongoClientURI connectionString = new MongoClientURI("mongodb://mongo-db1-waldorf.dev.aws.internal:27017");
        mongoClient = new MongoClient(connectionString);
    }

    @After
    public void removeTestDocuments() {
        requestsRepository.deleteById(WINTER_TEST_DOCUMENT);
        requestsRepository.deleteById(SUMMER_TEST_DOCUMENT);
    }

    @Ignore
    @Test
    public void testReasonDatesAreMidnightOnDateSpecifiedInMongoDB_InWinter()  throws JSONException {
        String documentId = WINTER_TEST_DOCUMENT;
        Instant.now(Clock.fixed(
            Instant.parse("2020-02-01T10:00:00Z"),
            ZoneOffset.UTC));
            createTestDocument(documentId);
         assess(documentId);
    }

    @Ignore
    @Test
    public void testReasonDatesAreMidnightOnDateSpecifiedInMongoDB_InSummer()  throws JSONException {
        String documentId = SUMMER_TEST_DOCUMENT;
        Instant.now(Clock.fixed(
            Instant.parse("2020-08-01T10:00:00Z"),
            ZoneOffset.UTC));
        createTestDocument(documentId);
        assess(documentId);
    }

    private void assess(String documentId) throws JSONException {
        JSONObject winter = queryMongoDbForReason(documentId, 0);
        JSONObject summer = queryMongoDbForReason(documentId, 1);
        assertEquals(formatter.format(
            winterStart.atTime(0,0,0)),
            winter.getJSONObject("startOn").getString("$date"));
        assertEquals(formatter.format(
            winterEnd.atTime(0,0,0)),
            winter.getJSONObject("endOn").getString("$date"));
        assertEquals(formatter.format(
            summerStart.atTime(0,0,0)),
            summer.getJSONObject("startOn").getString("$date"));
        assertEquals(formatter.format(
            summerEnd.atTime(0,0,0)),
            summer.getJSONObject("endOn").getString("$date"));
    }

    private void createTestDocument(String collectionId) {
        ExtensionRequestFullEntity entity = new ExtensionRequestFullEntity();
        entity.setId(collectionId);
        ExtensionReasonEntity reasonEntity1 = new ExtensionReasonEntity();
        reasonEntity1.setId("1");
        reasonEntity1.setReason("illness");
        reasonEntity1.setStartOn(winterStart);
        reasonEntity1.setEndOn(winterEnd);
        ExtensionReasonEntity reasonEntity2 = new ExtensionReasonEntity();
        reasonEntity2.setId("2");
        reasonEntity2.setReason("illness");
        reasonEntity2.setStartOn(summerStart);
        reasonEntity2.setEndOn(summerEnd);
        entity.setReasons(Arrays.asList(reasonEntity1, reasonEntity2));
        requestsRepository.save(entity);
    }

    private JSONObject queryMongoDbForReason(String documentId, int index) throws JSONException {
        MongoCollection<Document> collection = mongoClient.getDatabase("extension_requests").getCollection("extension_requests");
        Document document = collection.find(eq("_id", documentId)).first();

        String jsonRequestStr = document.toJson(JsonWriterSettings.builder().build());
        JSONObject request  = new JSONObject(jsonRequestStr);
        JSONArray reasons = request.getJSONArray("reasons");
        return reasons.getJSONObject(index);
    }
}
