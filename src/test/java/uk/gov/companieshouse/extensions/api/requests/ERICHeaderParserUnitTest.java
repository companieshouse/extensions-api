package uk.gov.companieshouse.extensions.api.requests;

import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.companieshouse.extensions.api.groups.Unit;

@Category(Unit.class)
@RunWith(MockitoJUnitRunner.class)
public class ERICHeaderParserUnitTest {

    private static final String ERIC_HEADER_IDENTITY = "ERIC-identity";
    private static final String ERIC_AUTHORISED_USER = "ERIC-Authorised-User";
    private static final String AUTH_USER = "demo@ch.gov.uk; forename=demoForename; surname=demoSurname";
    private static final String UTF8_AUTH_USER = "demo@ch.gov.uk; forename*=UTF-8''demo%20%3BForename; surname*=UTF-8''demo%3BSurname";
    private static final String ERIC_IDENTITY = "Y2VkZWVlMzhlZWFjY2M4MzQ3MT";

    private ERICHeaderParser ericHeaderParser = new ERICHeaderParser();

    @Mock
    private HttpServletRequest request;

    @Test
    public void testGetUserId() {
        when(request.getHeader(ERIC_HEADER_IDENTITY)).thenReturn(ERIC_IDENTITY);
        Assert.assertEquals(ERIC_IDENTITY, ericHeaderParser.getUserId(request));
    }

    @Test
    public void testGetUserId_null() {
        when(request.getHeader(ERIC_HEADER_IDENTITY)).thenReturn("");
        Assert.assertNull(ericHeaderParser.getUserId(request));
    }

    @Test
    public void testGetEmail() {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn(AUTH_USER);
        Assert.assertEquals("demo@ch.gov.uk", ericHeaderParser.getEmail(request));
    }

    @Test
    public void testGetEmail_UTF8() {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn(UTF8_AUTH_USER);
        Assert.assertEquals("demo@ch.gov.uk", ericHeaderParser.getEmail(request));
    }

    @Test
    public void testGetEmail_null() {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn("");
        Assert.assertNull(ericHeaderParser.getEmail(request));
    }

    @Test
    public void testGetForename() {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn(AUTH_USER);
        Assert.assertEquals("demoForename", ericHeaderParser.getForename(request));
    }

    @Test
    public void testGetForename_UTF8() {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn(UTF8_AUTH_USER);
        Assert.assertEquals("demo ;Forename", ericHeaderParser.getForename(request));
    }

    @Test
    public void testGetForename_null() {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn("");
        Assert.assertNull(ericHeaderParser.getForename(request));
    }

    @Test
    public void testGetSurname() {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn(AUTH_USER);
        Assert.assertEquals("demoSurname", ericHeaderParser.getSurname(request));
    }

    @Test
    public void testGetSurname_UTF8() {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn(UTF8_AUTH_USER);
        Assert.assertEquals("demo;Surname", ericHeaderParser.getSurname(request));
    }

    @Test
    public void testGetSurname_null() {
        when(request.getHeader(ERIC_AUTHORISED_USER)).thenReturn("");
        Assert.assertNull(ericHeaderParser.getSurname(request));
    }
}
