package uk.gov.companieshouse.extensions.api.logger;

import org.junit.BeforeClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.companieshouse.logging.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ApiLoggerTest {
    private static final String COMPANY_NUMBER_KEY = "company_number";
    private static final String THREAD_ID_KEY = "thread_id";
    private static final String TEST_MESSAGE = "hello";
    private static final String COMPANY_NUMBER = "12345678";
    private static final Map<String, Object> EXTRA_VALUES_MAP = new HashMap<String, Object>() {{
        put("my_key", "my_data");
    }};

    private static Logger mockLogger;
    private static ApiLogger apiLogger;

    @Captor
    private ArgumentCaptor<Map<String, Object>> mapArgumentCaptor;

    @BeforeClass
    public static void setupAllTests() throws Exception {
        apiLogger = new ApiLogger();

        mockLogger = mock(Logger.class);

        //Get the field to inject the mock into
        Field loggerField = ApiLogger.class.getDeclaredField("LOG");

        //make the field public
        loggerField.setAccessible(true);

        //make the field non final
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(loggerField, loggerField.getModifiers() & ~Modifier.FINAL);

        //set the new value - a mock version
        loggerField.set(null, mockLogger);

        //set the field back to private and final
        loggerField.setAccessible(false);
        modifiersField.setInt(loggerField, loggerField.getModifiers() & Modifier.FINAL);

        apiLogger.setCompanyNumber(COMPANY_NUMBER);
    }

    @BeforeEach
    public void setup() {
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
