package uk.gov.companieshouse.extensions.api.logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.extensions.api.requests.ERICHeaderParser;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
public class ApiLoggerTest {

    private static final String COMPANY_NUMBER_KEY = "company_number";
    private static final String THREAD_ID_KEY = "thread_id";
    private static final String TEST_MESSAGE = "hello";
    private static final String COMPANY_NUMBER = "12345678";

    private static final Map<String, Object> EXTRA_VALUES_MAP = new HashMap<String, Object>() {{
        put("my_key", "my_data");
    }};

    private static Logger mockLogger;
    private static ERICHeaderParser ericHeaderParser;
    private static ApiLogger apiLogger;

    @Captor
    private ArgumentCaptor<Map<String, Object>> mapArgumentCaptor;

    public static void setupAllTests() throws Exception {
        ericHeaderParser = mock(ERICHeaderParser.class);
        mockLogger = mock(Logger.class);

        apiLogger = new ApiLogger(mockLogger, ericHeaderParser);
        apiLogger.setCompanyNumber(COMPANY_NUMBER);
    }

    @BeforeEach
    public void setup() throws Exception {
        ApiLoggerTest.setupAllTests();
        Mockito.reset(mockLogger);
    }

    @Test
    public void testDebug() {
        apiLogger.debug(TEST_MESSAGE);
        verify(mockLogger, times(1)).debug(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertMapIsValid(mapArgumentCaptor);
    }

    @Test
    public void testDebugWithValues() {
        apiLogger.debug(TEST_MESSAGE, EXTRA_VALUES_MAP);
        verify(mockLogger, times(1)).debug(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertMapIsValid(mapArgumentCaptor, EXTRA_VALUES_MAP);
    }

    @Test
    public void testInfo() {
        apiLogger.info(TEST_MESSAGE);
        verify(mockLogger, times(1)).info(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertMapIsValid(mapArgumentCaptor);
    }

    @Test
    public void testInfoWithValues() {
        apiLogger.info(TEST_MESSAGE, EXTRA_VALUES_MAP);
        verify(mockLogger, times(1)).info(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertMapIsValid(mapArgumentCaptor, EXTRA_VALUES_MAP);
    }

    @Test
    public void testError() {
        apiLogger.error(TEST_MESSAGE);
        verify(mockLogger, times(1)).error(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertMapIsValid(mapArgumentCaptor);
    }

    @Test
    public void testErrorWithException() {
        Exception e = new Exception(TEST_MESSAGE);
        apiLogger.error(e);
        verify(mockLogger, times(1)).error(eq(TEST_MESSAGE), eq(e), mapArgumentCaptor.capture());

        assertMapIsValid(mapArgumentCaptor);
    }

    @Test
    public void testErrorWithExceptionAndMessage() {
        Exception e = new Exception("exception message");
        apiLogger.error("another message", e);
        verify(mockLogger, times(1)).error(eq("another message"), eq(e), mapArgumentCaptor.capture());

        assertMapIsValid(mapArgumentCaptor);
    }

    @Test
    public void testErrorWithValues() {
        apiLogger.error(TEST_MESSAGE, EXTRA_VALUES_MAP);
        verify(mockLogger, times(1)).error(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertMapIsValid(mapArgumentCaptor, EXTRA_VALUES_MAP);
    }

    /**
     * Check map contains default values
     *
     * @param mapArgumentCaptor
     */
    private void assertMapIsValid(ArgumentCaptor<Map<String, Object>> mapArgumentCaptor) {
        Map<String, Object> mapLogged = mapArgumentCaptor.getValue();
        assertTrue(mapLogged.containsKey(COMPANY_NUMBER_KEY));
        Assertions.assertEquals(COMPANY_NUMBER, mapLogged.get(COMPANY_NUMBER_KEY));
        assertTrue(mapLogged.containsKey(THREAD_ID_KEY));
        assertNotNull(mapLogged.get(THREAD_ID_KEY));
    }

    /**
     * Check map contains default + extra values
     *
     * @param mapArgumentCaptor
     * @param extraValues
     */
    private void assertMapIsValid(ArgumentCaptor<Map<String, Object>> mapArgumentCaptor, Map<String, Object> extraValues) {
        assertMapIsValid(mapArgumentCaptor);

        //check extra values beyond the defaults
        Map<String, Object> mapLogged = mapArgumentCaptor.getValue();
        extraValues.forEach((extraKey, extraValue) -> {
            assertTrue(mapLogged.containsKey(extraKey));
            Assertions.assertEquals(extraValue, mapLogged.get(extraKey));
        });
    }
}
