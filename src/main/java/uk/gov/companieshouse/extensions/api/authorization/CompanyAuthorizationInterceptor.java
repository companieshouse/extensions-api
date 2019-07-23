package uk.gov.companieshouse.extensions.api.authorization;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import uk.gov.companieshouse.extensions.api.logger.ApiLogger;

public class CompanyAuthorizationInterceptor extends HandlerInterceptorAdapter {

    private static final int COMPANY_NUMBER_LENGTH = 8;

    private ApiLogger logger;

    public CompanyAuthorizationInterceptor(ApiLogger logger) {
        this.logger = logger;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorizedScope = request.getHeader(AuthorizedRoles.ERIC_AUTHORISED_SCOPE);
        String authorizedCompany = authorizedScope.substring(
            Math.max(0, authorizedScope.length() - COMPANY_NUMBER_LENGTH));
        logger.debug("Company number from authorized scope: " + authorizedCompany);

        String actualCompany = ((Map<String, String>)request.getAttribute(
                                    HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                                      .get("companyNumber");

        logger.debug("Company number from path: " + actualCompany);
        if (actualCompany.equals(authorizedCompany)) {
            logger.debug("User with scope " + actualCompany + " has full authorization to proceed with request.");
            return true;
        }
        logger.debug("Company number does not match authorized scope" + 
                actualCompany + " =/= " + authorizedCompany);
        
        if (!HttpMethod.GET.matches(request.getMethod())) {
            logger.debug("Only a user with authorised company scope can modify a resource");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        if (adminCanGetResource(request)) {
            return true;
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return false;
    }

    private boolean hasPrivilege(HttpServletRequest request, String privilege) throws Exception {
        logger.debug("Checking admin privileges");
        return
            Arrays.stream(
                    Optional.ofNullable(request.getHeader(AuthorizedRoles.ERIC_AUTHORISED_ROLES))
                            .orElseThrow(() -> new Exception("Header missing: " + AuthorizedRoles.ERIC_AUTHORISED_ROLES))
                        .split(" "))
                .anyMatch(privilege::equals);
    }

    private boolean adminCanGetResource(HttpServletRequest request) {
        try {
            boolean viewPrivilege = hasPrivilege(request, AuthorizedRoles.ADMIN_VIEW);
            if (request.getRequestURI().endsWith("download")) {
                boolean downloadPrivilege = hasPrivilege(request, AuthorizedRoles.ADMIN_DOWNLOAD);
                if(downloadPrivilege && viewPrivilege) {
                    logger.debug("Admin download privileges detected, granting access to download resource");
                    return true;
                }
            } else if (viewPrivilege) {
                logger.debug("Admin view privilege detected, granting access to GET resource");
                return true;
            }
        } catch(Exception ex) {
            logger.error(ex);
        }
        return false;
    }
}