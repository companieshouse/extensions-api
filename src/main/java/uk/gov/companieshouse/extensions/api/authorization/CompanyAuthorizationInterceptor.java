package uk.gov.companieshouse.extensions.api.authorization;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import uk.gov.companieshouse.extensions.api.logger.ApiLogger;

@Component
public class CompanyAuthorizationInterceptor extends HandlerInterceptorAdapter {

    private static final int COMPANY_NUMBER_LENGTH = 8;

    @Autowired
    private ApiLogger logger;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorizedScope = request.getHeader("ERIC-Authorized-Scope");
        String authorizedCompany = authorizedScope.substring(
            Math.max(0, authorizedScope.length() - COMPANY_NUMBER_LENGTH));
        logger.debug("Company number from authorized scope: " + authorizedCompany);

        String actualCompany = ((Map<String, String>)request.getAttribute(
                                    HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                                      .get("companyNumber");

        logger.debug("Company number from path: " + actualCompany);
        if (actualCompany.equals(authorizedCompany)) {
            logger.debug("Company number does not match authorized scope" + 
                actualCompany + " =/= " + authorizedCompany);
            return true;
        }
        
        return HttpMethod.GET.matches(request.getMethod()) && 
            Arrays.stream(request.getHeader("ERIC-Authorized-Roles").split(" "))
                  .filter(role -> "/admin/extensions-view".equals(role))
                  .findAny()
                  .isPresent();
    }
}