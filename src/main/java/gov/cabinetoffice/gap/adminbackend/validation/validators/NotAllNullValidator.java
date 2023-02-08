package gov.cabinetoffice.gap.adminbackend.validation.validators;

import gov.cabinetoffice.gap.adminbackend.annotations.NotAllNull;
import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Objects;

public class NotAllNullValidator implements ConstraintValidator<NotAllNull, Object> {

    private String[] fields;

    @Override
    public void initialize(NotAllNull notAllNull) {
        fields = notAllNull.fields();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        final BeanWrapperImpl beanWrapper = new BeanWrapperImpl(object);

        return Arrays.stream(fields).map(beanWrapper::getPropertyValue).filter(Objects::isNull).count() < fields.length;
    }

}
