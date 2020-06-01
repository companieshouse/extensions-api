package uk.gov.companieshouse.extensions.api.requests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class ERICHeaderParserUnitTest {

    private static final String ERIC_HEADER_IDENTITY = "ERIC-identity";
    private static final String ERIC_AUTHORISED_USER = "ERIC-Authorised-User";
    private static final String AUTH_USER = "demo@ch.gov.uk; forename=demoForename; surname=demoSurname";
    private static final String UTF8_AUTH_USER = "demo@ch.gov.uk; forename*=UTF-8''demo%20%3BForename; surname*=UTF-8''demo%3BSurname";
    private static final String ERIC_IDENTITY = "Y2VkZWVlMzhlZWFjY2M4MzQ3MT";

    private final ERICHeaderParser ericHeaderParser = new ERICHeaderParser();

    @Mock
    private HttpServletRequest request;

    @Test
    public void testGetUserId() {
        when(request.getHeader(ERIC_HEADER_IDENTITY)).thenReturn(ERIC_IDENTITY);
        assertEquals(ERIC_IDENTITY, ericHeaderParser.getUserId(request));
    }

    @Test
    public void testGetUserId_null() {
        when(request.getHeader(ERIC_HEADER_IDENTITY)).thenReturn("");
        assertNull(ericHeaderParser.getUserId(request));
    }

    @Test
    public void testGetEmail() {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn(AUTH_USER);
        assertEquals("demo@ch.gov.uk", ericHeaderParser.getEmail(request));
    }

    @Test
    public void testGetEmail_UTF8() {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn(UTF8_AUTH_USER);
        assertEquals("demo@ch.gov.uk", ericHeaderParser.getEmail(request));
    }

    @Test
    public void testGetEmail_null() {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn("");
        assertNull(ericHeaderParser.getEmail(request));
    }

    @Test
    public void testGetForename() throws UnsupportedEncodingException {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn(AUTH_USER);
        assertEquals("demoForename", ericHeaderParser.getForename(request));
    }

    @Test
    public void testGetForename_UTF8() throws UnsupportedEncodingException {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn(UTF8_AUTH_USER);
        assertEquals("demo ;Forename", ericHeaderParser.getForename(request));
    }

    @Test
    public void testGetForename_null() throws UnsupportedEncodingException {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn("");
        assertNull(ericHeaderParser.getForename(request));
    }

    @Test
    public void testGetSurname() throws UnsupportedEncodingException {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn(AUTH_USER);
        assertEquals("demoSurname", ericHeaderParser.getSurname(request));
    }

    @Test
    public void testGetSurname_UTF8() throws UnsupportedEncodingException {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn(UTF8_AUTH_USER);
        assertEquals("demo;Surname", ericHeaderParser.getSurname(request));
    }

    @Test
    public void testGetSurname_null() throws UnsupportedEncodingException {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn("");
        assertNull(ericHeaderParser.getSurname(request));
    }
}
