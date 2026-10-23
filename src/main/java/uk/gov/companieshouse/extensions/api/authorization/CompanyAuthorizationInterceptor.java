package uk.gov.companieshouse.extensions.api.authorization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.RequestsService;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Authorisation rules, based on the identity ERIC has authenticated:
 * <ul>
 *   <li>admins (roles present) can only download attachments;</li>
 *   <li>oauth2 users can create requests and read/write their own, but cannot download;</li>
 *   <li>key identities can list/read requests and update a request status.</li>
 * </ul>
 */
public class CompanyAuthorizationInterceptor implements HandlerInterceptor {

    public static final String OWNER_FILTER_ATTRIBUTE = "extensions.ownerFilter";

    private static final String ERIC_IDENTITY = "ERIC-Identity";
    private static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    private static final String OAUTH2 = "oauth2";
    private static final String KEY = "key";
    private static final Pattern URI_PATTERN =
        Pattern.compile("^/company/([^/]+)/extensions/requests(?:/([^/]+))?(/.*)?$");

    private final ApiLogger logger;
    private final RequestsService requestsService;

    public CompanyAuthorizationInterceptor(ApiLogger logger, RequestsService requestsService) {
        this.logger = logger;
        this.requestsService = requestsService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String identity = request.getHeader(ERIC_IDENTITY);
        if (StringUtils.isBlank(identity)) {
            return deny(request, response, HttpStatus.UNAUTHORIZED, "Missing ERIC identity");
        }

        Matcher uri = URI_PATTERN.matcher(Optional.ofNullable(request.getRequestURI()).orElse(""));
        if (!uri.matches()) {
            return deny(request, response, HttpStatus.FORBIDDEN, "Unrecognised resource");
        }

        if (Optional.ofNullable(uri.group(3)).orElse("").endsWith("/download")) {
            return authoriseDownload(request, response);
        }

        if (hasRole(request, AuthorizedRoles.ADMIN_VIEW) || hasRole(request, AuthorizedRoles.ADMIN_DOWNLOAD)) {
            return deny(request, response, HttpStatus.FORBIDDEN, "Admins can only download attachments");
        }

        String identityType = request.getHeader(ERIC_IDENTITY_TYPE);
        return uri.group(2) == null
            ? authoriseCollection(request, response, identity, identityType)
            : authoriseResource(request, response, identity, identityType, uri);
    }

    private boolean authoriseDownload(HttpServletRequest request, HttpServletResponse response) {
        boolean permitted = isGet(request)
            && hasRole(request, AuthorizedRoles.ADMIN_VIEW)
            && hasRole(request, AuthorizedRoles.ADMIN_DOWNLOAD);

        return permitted
            ? allow(request, "Admin download permitted")
            : deny(request, response, HttpStatus.FORBIDDEN, "Download not permitted");
    }

    private boolean authoriseCollection(HttpServletRequest request, HttpServletResponse response, String identity,
        String identityType) {
        if (KEY.equals(identityType) && isGet(request)) {
            return allow(request, "Key identity list permitted");
        }

        if (OAUTH2.equals(identityType) && (isGet(request) || HttpMethod.POST.matches(request.getMethod()))) {
            request.setAttribute(OWNER_FILTER_ATTRIBUTE, identity);
            return allow(request, "User permitted");
        }

        return deny(request, response, HttpStatus.FORBIDDEN, "Not authorised for this resource");
    }

    private boolean authoriseResource(HttpServletRequest request, HttpServletResponse response, String identity,
        String identityType, Matcher uri) {
        Optional<ExtensionRequestFullEntity> extensionRequest =
            requestsService.getExtensionsRequestById(uri.group(1), uri.group(2));
        if (extensionRequest.isEmpty()) {
            return deny(request, response, HttpStatus.NOT_FOUND, "Request not found");
        }

        boolean statusUpdate = HttpMethod.PATCH.matches(request.getMethod())
            && (uri.group(3) == null || "/".equals(uri.group(3)));
        if (statusUpdate) {
            return KEY.equals(identityType)
                ? allow(request, "Key identity status update permitted")
                : deny(request, response, HttpStatus.FORBIDDEN, "Status update requires a key identity");
        }

        if (OAUTH2.equals(identityType) && isOwner(extensionRequest.get(), identity)) {
            return allow(request, "Owner permitted");
        }

        if (KEY.equals(identityType) && isGet(request)) {
            return allow(request, "Key identity read permitted");
        }

        return deny(request, response, HttpStatus.FORBIDDEN, "Not authorised for this resource");
    }

    private boolean isOwner(ExtensionRequestFullEntity entity, String identity) {
        return entity.getCreatedBy() != null && identity.equals(entity.getCreatedBy().getId());
    }

    private boolean isGet(HttpServletRequest request) {
        return HttpMethod.GET.matches(request.getMethod());
    }

    private boolean hasRole(HttpServletRequest request, String role) {
        String roles = request.getHeader(AuthorizedRoles.ERIC_AUTHORISED_ROLES);
        return roles != null && Arrays.asList(roles.split(" ")).contains(role);
    }

    private boolean allow(HttpServletRequest request, String message) {
        logger.debug(message, request);
        return true;
    }

    private boolean deny(HttpServletRequest request, HttpServletResponse response, HttpStatus status,
        String message) {
        logger.debug(message, request);
        response.setStatus(status.value());
        return false;
    }
}
