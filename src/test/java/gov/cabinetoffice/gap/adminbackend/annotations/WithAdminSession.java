package gov.cabinetoffice.gap.adminbackend.annotations;

import gov.cabinetoffice.gap.adminbackend.security.WithAdminSessionSecurityContextFactory;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithAdminSessionSecurityContextFactory.class,
        setupBefore = TestExecutionEvent.TEST_EXECUTION)
public @interface WithAdminSession {

    int grantAdminId() default 1;

    int funderId() default 1;

}
