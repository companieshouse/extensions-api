package uk.gov.companieshouse.extensions.api.requests;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.UnsupportedEncodingException;

import static org.mockito.Mockito.when;

@Tag("UnitTest")
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
        Assertions.assertEquals(ERIC_IDENTITY, ericHeaderParser.getUserId(request));
    }

    @Test
    public void testGetUserId_null() {
        when(request.getHeader(ERIC_HEADER_IDENTITY)).thenReturn("");
        Assertions.assertNull(ericHeaderParser.getUserId(request));
    }

    @Test
    public void testGetEmail() {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn(AUTH_USER);
        Assertions.assertEquals("demo@ch.gov.uk", ericHeaderParser.getEmail(request));
    }

    @Test
    public void testGetEmail_UTF8() {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn(UTF8_AUTH_USER);
        Assertions.assertEquals("demo@ch.gov.uk", ericHeaderParser.getEmail(request));
    }

    @Test
    public void testGetEmail_null() {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn("");
        Assertions.assertNull(ericHeaderParser.getEmail(request));
    }

    @Test
    public void testGetForename() throws UnsupportedEncodingException {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn(AUTH_USER);
        Assertions.assertEquals("demoForename", ericHeaderParser.getForename(request));
    }

    @Test
    public void testGetForename_UTF8() throws UnsupportedEncodingException {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn(UTF8_AUTH_USER);
        Assertions.assertEquals("demo ;Forename", ericHeaderParser.getForename(request));
    }

    @Test
    public void testGetForename_null() throws UnsupportedEncodingException {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn("");
        Assertions.assertNull(ericHeaderParser.getForename(request));
    }

    @Test
    public void testGetSurname() throws UnsupportedEncodingException {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn(AUTH_USER);
        Assertions.assertEquals("demoSurname", ericHeaderParser.getSurname(request));
    }

    @Test
    public void testGetSurname_UTF8() throws UnsupportedEncodingException {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn(UTF8_AUTH_USER);
        Assertions.assertEquals("demo;Surname", ericHeaderParser.getSurname(request));
    }

    @Test
    public void testGetSurname_null() throws UnsupportedEncodingException {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn("");
        Assertions.assertNull(ericHeaderParser.getSurname(request));
    }
}
