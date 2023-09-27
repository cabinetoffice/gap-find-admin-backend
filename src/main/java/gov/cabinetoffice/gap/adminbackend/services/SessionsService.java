package gov.cabinetoffice.gap.adminbackend.services;

import gov.cabinetoffice.gap.adminbackend.dtos.errors.FieldErrorsDTO;
import gov.cabinetoffice.gap.adminbackend.enums.SessionObjectEnum;
import gov.cabinetoffice.gap.adminbackend.models.ValidationError;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SessionsService {

    public HashMap<String, String> retrieveObjectFromSession(SessionObjectEnum objectKey, HttpSession session) {
        Iterator<String> sessionAttributesIterator = session.getAttributeNames().asIterator();
        HashMap<String, String> returnObj = new HashMap<String, String>();

        while (sessionAttributesIterator.hasNext()) {
            String attributeName = sessionAttributesIterator.next();
            if (attributeName.startsWith(objectKey.toString() + ".")) { // if the
                                                                        // attribute
                                                                        // starts with the
                                                                        // given object
                                                                        // key and a dot,
                                                                        // ie.
                                                                        // 'newScheme.name'
                String fieldName = HelperUtils.getSessionObjectFieldName(attributeName); // retain
                                                                                         // just
                                                                                         // the
                                                                                         // section
                                                                                         // after
                                                                                         // the
                                                                                         // dot
                                                                                         // (the
                                                                                         // field
                                                                                         // name),
                                                                                         // ie.
                                                                                         // 'name'
                returnObj.put(fieldName, (String) session.getAttribute(attributeName));
            }
        }

        return returnObj;
    }

    public boolean deleteObjectFromSession(SessionObjectEnum objectKey, HttpSession session) {
        Iterator<String> sessionAttributesIterator = session.getAttributeNames().asIterator();
        boolean valuesFound = false;
        while (sessionAttributesIterator.hasNext()) {
            String attributeName = sessionAttributesIterator.next();
            if (attributeName.startsWith(objectKey.toString() + ".")) {
                valuesFound = true;
                session.removeAttribute(attributeName);
            }
        }

        return valuesFound;
    }

    @SneakyThrows
    public FieldErrorsDTO validateFieldOnDto(String key, String value, HttpSession session) {
        // validate the value against its corresponding DTO field
        for (SessionObjectEnum val : SessionObjectEnum.values()) {
            if (key.startsWith(val + ".")) {
                Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
                String strippedKey = HelperUtils.getSessionObjectFieldName(key);
                // if the field is an enum, we need to manually validate it's one of the
                // options,
                // by calling valueOf on the enum and catching any exceptions. yes, I know
                // it's kinda gross
                if (val.getClassType().getDeclaredField(strippedKey).getType().isEnum()) {
                    try {
                        val.getClassType().getDeclaredField(strippedKey).getType().getMethod("valueOf", String.class)
                                .invoke(null, value); // method is static, so can be
                                                      // called without a class
                        session.setAttribute(key, value);
                        return null;
                    }
                    catch (Exception e) {
                        return new FieldErrorsDTO(Collections.singletonList(new ValidationError(strippedKey,
                                value + " is not a possible value of " + val.getClassType().getSimpleName())));
                    }
                }
                else {
                    Set<? extends ConstraintViolation<?>> constraintViolationsSet = validator.validateValue(val.getClassType(),
                            strippedKey, value);
                    if (constraintViolationsSet.isEmpty()) {
                        session.setAttribute(key, value);
                        return null;
                    }
                    else {
                        return new FieldErrorsDTO(constraintViolationsSet.stream()
                                .map((err) -> new ValidationError(err.getPropertyPath().toString(), err.getMessage()))
                                .toList());
                    }
                }
            }
        }

        // if it never matched an enum value, no validation required
        session.setAttribute(key, value);
        return null;
    }

    public FieldErrorsDTO validateSessionObject(final SessionObjectEnum objectKey, final Map<String, String> object,
            final HttpSession session) {
        return new FieldErrorsDTO(object.entrySet().stream()
                .map(sessionValue -> validateFieldOnDto(objectKey + "." + sessionValue.getKey(),
                        sessionValue.getValue(), session))
                .filter(Objects::nonNull).flatMap(fieldErrorsDTO -> fieldErrorsDTO.getFieldErrors().stream()).toList());
    }

}