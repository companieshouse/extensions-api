package uk.gov.companieshouse.extensions.api.logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.extensions.api.requests.ERICHeaderParser;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
public class ApiLoggerTest {

    private static final String COMPANY_NUMBER_KEY = "company_number";
    private static final String THREAD_ID_KEY = "thread_id";
    private static final String TEST_MESSAGE = "hello";
    private static final String COMPANY_NUMBER = "12345678";

    private static final Map<String, Object> EXTRA_VALUES_MAP = new HashMap<>();

    @Mock
    private static Logger mockLogger;

    @Mock
    private static ERICHeaderParser ericHeaderParser;

    @Captor
    private ArgumentCaptor<Map<String, Object>> mapArgumentCaptor;

    @InjectMocks
    private static ApiLogger underTest;

    @BeforeEach
    public void setup() throws Exception {
        EXTRA_VALUES_MAP.put("my_key", "my_data");

        underTest.setCompanyNumber(COMPANY_NUMBER);
    }

    @AfterEach
    public void tearDown() {
        EXTRA_VALUES_MAP.clear();

        underTest.removeCompanyNumber();
    }

    @Test
    @Disabled
    public void testCompanyNumberSet() {

    }

    @Test
    public void testDebug() {
        underTest.debug(TEST_MESSAGE);

        verify(mockLogger, times(1)).debug(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertDefaultMapIsValid(mapArgumentCaptor);
    }

    @Test
    public void testDebugWithValues() {
        underTest.debug(TEST_MESSAGE, EXTRA_VALUES_MAP);

        verify(mockLogger, times(1)).debug(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertDefaultMapIsValid(mapArgumentCaptor, EXTRA_VALUES_MAP);
    }

    @Test
    public void testInfo() {
        underTest.info(TEST_MESSAGE);

        verify(mockLogger, times(1)).info(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertDefaultMapIsValid(mapArgumentCaptor);
    }

    @Test
    public void testInfoWithValues() {
        underTest.info(TEST_MESSAGE, EXTRA_VALUES_MAP);

        verify(mockLogger, times(1)).info(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertDefaultMapIsValid(mapArgumentCaptor, EXTRA_VALUES_MAP);
    }

    @Test
    public void testError() {
        underTest.error(TEST_MESSAGE);

        verify(mockLogger, times(1)).error(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertDefaultMapIsValid(mapArgumentCaptor);
    }

    @Test
    public void testErrorWithException() {
        Exception e = new Exception(TEST_MESSAGE);
        underTest.error(e);

        verify(mockLogger, times(1)).error(eq(TEST_MESSAGE), eq(e), mapArgumentCaptor.capture());

        assertDefaultMapIsValid(mapArgumentCaptor);
    }

    @Test
    public void testErrorWithExceptionAndMessage() {
        Exception e = new Exception("exception message");
        underTest.error("another message", e);

        verify(mockLogger, times(1)).error(eq("another message"), eq(e), mapArgumentCaptor.capture());

        assertDefaultMapIsValid(mapArgumentCaptor);
    }

    @Test
    public void testErrorWithValues() {
        underTest.error(TEST_MESSAGE, EXTRA_VALUES_MAP);

        verify(mockLogger, times(1)).error(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertDefaultMapIsValid(mapArgumentCaptor, EXTRA_VALUES_MAP);
    }

    /**
     * Check map contains default values
     */
    private void assertDefaultMapIsValid(ArgumentCaptor<Map<String, Object>> mapArgumentCaptor) {
        Map<String, Object> mapLogged = mapArgumentCaptor.getValue();
        assertNotNull(mapLogged);

        assertTrue(mapLogged.containsKey(COMPANY_NUMBER_KEY));
        assertEquals(COMPANY_NUMBER, mapLogged.get(COMPANY_NUMBER_KEY));
        assertTrue(mapLogged.containsKey(THREAD_ID_KEY));
        assertNotNull(mapLogged.get(THREAD_ID_KEY));
    }

    /**
     * Check map contains default + extra values
     */
    private void assertDefaultMapIsValid(ArgumentCaptor<Map<String, Object>> mapArgumentCaptor, Map<String, Object> extraValues) {
        assertDefaultMapIsValid(mapArgumentCaptor);

        //check extra values beyond the defaults
        Map<String, Object> mapLogged = mapArgumentCaptor.getValue();
        assertNotNull(mapLogged);

        extraValues.forEach((extraKey, extraValue) -> {
            assertTrue(mapLogged.containsKey(extraKey));
            assertEquals(extraValue, mapLogged.get(extraKey));
        });
    }
}
