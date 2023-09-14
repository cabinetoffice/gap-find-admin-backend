package gov.cabinetoffice.gap.adminbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtTokenFilterConfig {

    @Value("${feature.onelogin.enabled}")
    public boolean oneLoginEnabled;

    @Value("${feature.validate-user-roles-in-middleware}")
    public boolean validateUserRolesInMiddleware;

}
