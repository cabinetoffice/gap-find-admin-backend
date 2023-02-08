package gov.cabinetoffice.gap.adminbackend.validation.annotations;

import gov.cabinetoffice.gap.adminbackend.validation.validators.AdvertPageResponseValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Constraint(validatedBy = { AdvertPageResponseValidator.class })
@Target({ TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPageResponse {

    String message() default "Invalid value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
