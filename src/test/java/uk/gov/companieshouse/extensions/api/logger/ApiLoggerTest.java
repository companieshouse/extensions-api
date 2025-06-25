package uk.gov.companieshouse.extensions.api.logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.extensions.api.requests.ERICHeaderParser;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class ApiLoggerTest {

    private static final String COMPANY_NUMBER_KEY = "company_number";
    private static final String THREAD_ID_KEY = "thread_id";
    private static final String TEST_MESSAGE = "hello";
    private static final String COMPANY_NUMBER = "12345678";

    private static final Map<String, Object> EXTRA_VALUES_MAP = new HashMap<>() {{
        put("my_key", "my_data");
    }};

    @Mock
    private static Logger logger;

    @Mock
    private static ERICHeaderParser ericHeaderParser;

    @Captor
    private ArgumentCaptor<Map<String, Object>> mapArgumentCaptor;

    private static ApiLogger underTest;

    @BeforeEach
    void setup() {
        underTest = new ApiLogger(logger, ericHeaderParser);
        underTest.setCompanyNumber(COMPANY_NUMBER);
    }

    @Test
    void testConstructor() {
        ApiLogger apiLogger = new ApiLogger(logger, ericHeaderParser);
        assertNotNull(apiLogger);
    }

    @Test
    void testSetCompanyNumber() {
        underTest.removeCompanyNumber();

        Map<String, Object> internalMap = underTest.getInternalDataMap();
        assertTrue(internalMap.containsKey(COMPANY_NUMBER_KEY));
        assertNull(internalMap.get(COMPANY_NUMBER_KEY));

        underTest.setCompanyNumber(COMPANY_NUMBER);

        Map<String, Object> updatedMap = underTest.getInternalDataMap();
        assertTrue(updatedMap.containsKey(COMPANY_NUMBER_KEY));
        assertEquals(COMPANY_NUMBER, updatedMap.get(COMPANY_NUMBER_KEY));
    }

    @Test
    void testRemoveCompanyNumber() {
        Map<String, Object> internalMap = underTest.getInternalDataMap();
        assertTrue(internalMap.containsKey(COMPANY_NUMBER_KEY));
        assertEquals(COMPANY_NUMBER, internalMap.get(COMPANY_NUMBER_KEY));

        underTest.removeCompanyNumber();

        Map<String, Object> updatedMap = underTest.getInternalDataMap();
        assertTrue(updatedMap.containsKey(COMPANY_NUMBER_KEY));
        assertNull(updatedMap.get(COMPANY_NUMBER_KEY));
    }

    @Test
    void testInternalDataMap() {
        Map<String, Object> internalMap = underTest.getInternalDataMap();
        assertNotNull(internalMap);

        assertTrue(internalMap.containsKey(COMPANY_NUMBER_KEY));
        assertTrue(internalMap.containsKey(THREAD_ID_KEY));

        assertEquals(COMPANY_NUMBER, internalMap.get(COMPANY_NUMBER_KEY));
        assertEquals(1L, internalMap.get(THREAD_ID_KEY));
    }

    @Test
    void testDebug() {
        underTest.debug(TEST_MESSAGE);

        verify(logger, times(1)).debug(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertInternalMapIsValid();
    }

    @Test
    void testDebugWithValues() {
        underTest.debug(TEST_MESSAGE, EXTRA_VALUES_MAP);

        verify(logger, times(1)).debug(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertExtraValuesMapIsValid();
    }

    @Test
    void testDebugWithRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(ericHeaderParser.getUserId(request)).thenReturn("user123");

        underTest.debug(TEST_MESSAGE, request);

        verify(ericHeaderParser, times(1)).getUserId(request);
        verify(logger, times(1)).debug(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertInternalMapIsValid();
    }

    @Test
    void testInfo() {
        underTest.info(TEST_MESSAGE);

        verify(logger, times(1)).info(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertInternalMapIsValid();
    }

    @Test
    void testInfoWithValues() {
        underTest.info(TEST_MESSAGE, EXTRA_VALUES_MAP);

        verify(logger, times(1)).info(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertExtraValuesMapIsValid();
    }

    @Test
    void testError() {
        underTest.error(TEST_MESSAGE);

        verify(logger, times(1)).error(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertInternalMapIsValid();
    }

    @Test
    void testErrorWithException() {
        Exception e = new Exception(TEST_MESSAGE);
        underTest.error(e);

        verify(logger, times(1)).error(eq(TEST_MESSAGE), eq(e), mapArgumentCaptor.capture());

        assertInternalMapIsValid();
    }

    @Test
    void testErrorWithExceptionAndMessage() {
        Exception e = new Exception("exception message");
        underTest.error("another message", e);

        verify(logger, times(1)).error(eq("another message"), eq(e), mapArgumentCaptor.capture());

        assertInternalMapIsValid();
    }

    @Test
    void testErrorWithValues() {
        underTest.error(TEST_MESSAGE, EXTRA_VALUES_MAP);

        verify(logger, times(1)).error(eq(TEST_MESSAGE), mapArgumentCaptor.capture());

        assertExtraValuesMapIsValid();
    }

    /**
     * Check map contains default values
     */
    private void assertInternalMapIsValid() {
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
    private void assertExtraValuesMapIsValid() {
        assertInternalMapIsValid();

        //check extra values beyond the defaults
        Map<String, Object> mapLogged = mapArgumentCaptor.getValue();
        assertNotNull(mapLogged);

        EXTRA_VALUES_MAP.forEach((extraKey, extraValue) -> {
            assertTrue(mapLogged.containsKey(extraKey));
            assertEquals(extraValue, mapLogged.get(extraKey));
        });
    }
}
