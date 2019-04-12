package uk.gov.companieshouse.extensions.api.requests;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Component
public class ERICHeaderParser {
    private static final String ERIC_FORENAME = "forename=";
    private static final String ERIC_FORENAME_UTF8 = "forename*=";
    private static final String ERIC_SURNAME = "surname=";
    private static final String ERIC_SURNAME_UTF8 = "surname*=";
    private static final String ERIC_IDENTITY = "ERIC-identity";
    private static final String DELIMITER = ";";
    private static final String ERIC_AUTHORISED_USER = "ERIC-Authorised-User";
    private static final String EMAIL_IDENTIFIER = "@";

    public String getUserId(HttpServletRequest request) {
        String userId = request.getHeader(ERIC_IDENTITY);
        if (StringUtils.isNotBlank(userId)) {
            return userId;
        } else {
            return null;
        }
    }

    public String getEmail(HttpServletRequest request) {
        String email = null;
        String ericAuthorisedUser = getERICAuthorisedUser(request);
        String[] values = ericAuthorisedUser.split(DELIMITER);
        //email should be first value in the string
        if (values.length > 0) {
            String firstValue = values[0];
            if (firstValue.contains(EMAIL_IDENTIFIER)) {
                email = firstValue;
            }
        }
        return email;
    }

    public String getForename(HttpServletRequest request) {
        String forename = getFromAuthorisedUser(request, ERIC_FORENAME, DELIMITER);
        if (forename == null) {
            forename = getFromAuthorisedUser(request, ERIC_FORENAME_UTF8, DELIMITER);
            if (forename != null) {
                forename = decodeUTF8(forename);
            }
        }
        return forename;
    }

    public String getSurname(HttpServletRequest request) {
        String surname = getFromAuthorisedUser(request, ERIC_SURNAME, null);
        if (surname == null) {
            surname = getFromAuthorisedUser(request, ERIC_SURNAME_UTF8, null);
            if (surname != null) {
                surname = decodeUTF8(surname);
            }
        }
        return surname;
    }

    private String getERICAuthorisedUser(HttpServletRequest request) {
        return request.getHeader(ERIC_AUTHORISED_USER);
    }

    private String getFromAuthorisedUser(HttpServletRequest request, String key, String delimiter) {
        String ericAuthorisedUser = getERICAuthorisedUser(request);
        String name = null;
        int nameStartIndex = ericAuthorisedUser.indexOf(key);

        if (nameStartIndex >= 0) {
            nameStartIndex += key.length();
            if (delimiter == null) {
                name = ericAuthorisedUser.substring(nameStartIndex);
            } else {
                int nameEndIndex = ericAuthorisedUser.indexOf(delimiter, nameStartIndex);
                if (nameEndIndex >= nameStartIndex) {
                    name = ericAuthorisedUser.substring(nameStartIndex, nameEndIndex);
                }
            }
        }
        return name;
    }

    private String decodeUTF8(String utf8String) {
        String decoded = null;
        String utf8Prefix = "UTF-8''";
        utf8String = StringUtils.remove(utf8String, utf8Prefix);
        try {
            decoded = URLDecoder.decode(utf8String, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO log exception - this should never happen if the "UTF-8" encoding type is correct
        }
        return decoded;
    }

}
