package gov.cabinetoffice.gap.adminbackend.validation.validators;

import gov.cabinetoffice.gap.adminbackend.annotations.NotAllNull;
import gov.cabinetoffice.gap.adminbackend.dtos.schemes.SchemePatchDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotAllNullValidatorTest {

    @Mock
    private NotAllNull notAllNull;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Test
    void testNameValidatorisValid_AllValuesNull() {

        when(notAllNull.fields()).thenReturn(new String[] { "name", "ggisReference" });

        NotAllNullValidator notAllNullValidator = new NotAllNullValidator();
        notAllNullValidator.initialize(notAllNull);

        boolean isValid = notAllNullValidator.isValid(new SchemePatchDTO(null, null, null), constraintValidatorContext);
        assertFalse(isValid);
    }

    @Test
    void testNameValidatorisValid_AllValuesPopulated() {

        when(notAllNull.fields()).thenReturn(new String[] { "name", "ggisReference" });

        NotAllNullValidator notAllNullValidator = new NotAllNullValidator();
        notAllNullValidator.initialize(notAllNull);

        boolean isValid = notAllNullValidator.isValid(
                new SchemePatchDTO("scheme name", "ggis reference number", "contact@email.com"),
                constraintValidatorContext);
        assertTrue(isValid);
    }

    @Test
    void testNameValidatorisValid_SomeValuesPopulated() {

        when(notAllNull.fields()).thenReturn(new String[] { "name", "ggisReference" });

        NotAllNullValidator notAllNullValidator = new NotAllNullValidator();
        notAllNullValidator.initialize(notAllNull);

        boolean isValid = notAllNullValidator.isValid(new SchemePatchDTO("sample name", null, null),
                constraintValidatorContext);
        assertTrue(isValid);
    }

}
