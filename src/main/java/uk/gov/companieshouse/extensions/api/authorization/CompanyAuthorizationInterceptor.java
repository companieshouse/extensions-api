package uk.gov.companieshouse.extensions.api.authorization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.service.ServiceException;

import java.util.Arrays;
import java.util.Optional;

public class CompanyAuthorizationInterceptor implements HandlerInterceptor {

    private ApiLogger logger;

    public CompanyAuthorizationInterceptor(ApiLogger logger) {
        this.logger = logger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return true;
//        if (!HttpMethod.GET.matches(request.getMethod())) {
//            if (StringUtils.isEmpty(request.getHeader(AuthorizedRoles.ERIC_AUTHORISED_ROLES))) {
//                logger.debug("User is permitted to update attachment", request);
//                return true;
//            } else {
//                logger.debug("Admin user is not permitted to modify an attachment", request);
//                response.setStatus(HttpStatus.UNAUTHORIZED.value());
//                return false;
//            }
//        }
//
//        if (adminCanGetResource(request)) {
//            return true;
//        }
//
//        logger.debug("User is not authorized to view this resource", request);
//        response.setStatus(HttpStatus.UNAUTHORIZED.value());
//        return false;
    }

    private boolean hasPrivilege(HttpServletRequest request, String privilege) throws ServiceException {
        logger.debug("Checking admin privileges", request);
        return
            Arrays.stream(
                    Optional.ofNullable(request.getHeader(AuthorizedRoles.ERIC_AUTHORISED_ROLES))
                        .orElseThrow(() -> new ServiceException("Header missing: " + AuthorizedRoles.ERIC_AUTHORISED_ROLES))
                        .split(" "))
                .anyMatch(privilege::equals);
    }

    private boolean adminCanGetResource(HttpServletRequest request) {
        try {
            boolean viewPrivilege = hasPrivilege(request, AuthorizedRoles.ADMIN_VIEW);
            if (request.getRequestURI().endsWith("download")) {
                boolean downloadPrivilege = hasPrivilege(request, AuthorizedRoles.ADMIN_DOWNLOAD);
                if (downloadPrivilege && viewPrivilege) {
                    logger.debug("Admin download privileges detected, granting access to download resource", request);
                    return true;
                }
            } else if (viewPrivilege) {
                logger.debug("Admin view privilege detected, granting access to GET resource", request);
                return true;
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        return false;
    }
}
