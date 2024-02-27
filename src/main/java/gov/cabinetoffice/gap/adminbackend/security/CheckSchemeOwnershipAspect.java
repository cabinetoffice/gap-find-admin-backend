package gov.cabinetoffice.gap.adminbackend.security;

import gov.cabinetoffice.gap.adminbackend.dtos.application.ApplicationFormNoSections;
import gov.cabinetoffice.gap.adminbackend.entities.GrantAdvert;
import gov.cabinetoffice.gap.adminbackend.entities.SchemeEntity;
import gov.cabinetoffice.gap.adminbackend.exceptions.NotFoundException;
import gov.cabinetoffice.gap.adminbackend.models.AdminSession;
import gov.cabinetoffice.gap.adminbackend.repositories.ApplicationFormRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.GrantAdvertRepository;
import gov.cabinetoffice.gap.adminbackend.repositories.SchemeRepository;
import gov.cabinetoffice.gap.adminbackend.utils.HelperUtils;
import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Aspect
@Component
@AllArgsConstructor
public class CheckSchemeOwnershipAspect {

    public final SchemeRepository schemeRepository;
    public final GrantAdvertRepository grantAdvertRepository;
    public final ApplicationFormRepository applicationFormRepository;

    @Pointcut("@annotation(gov.cabinetoffice.gap.adminbackend.security.CheckSchemeOwnership)")
    private void checkSchemeOwnershipPointcut() {
    }

    @Before("@annotation(checkSchemeOwnership)")
    public void checkSchemeOwnershipBefore(JoinPoint joinPoint, CheckSchemeOwnership checkSchemeOwnership) {
        AdminSession session = HelperUtils.getAdminSessionForAuthenticatedUser();

        Map<String, Object> methodArgumentsMap = getMethodArguments(joinPoint);
        Integer schemeId = fetchSchemeIdFromMethodArgs(methodArgumentsMap);

        SchemeEntity scheme = schemeRepository.findById(schemeId).orElseThrow();

        boolean isSchemeEditor = scheme.getGrantAdmins().stream().anyMatch(grantAdmin -> grantAdmin.getId()
                .equals(session.getGrantAdminId()));

        if (!isSchemeEditor) {
            throw new AccessDeniedException(String.format("User with sub %s does not have access to scheme with id %s",
                    session.getGrantAdminId(), schemeId));
        }
    }

    private interface SchemeIdExtractor {
        Integer extractSchemeId(Object obj);
    }

    private final Map<String, SchemeIdExtractor> EXTRACTORS = Map.of(
            "schemeId", this::castResponseIdToInteger,
            "grantSchemeId", this::castResponseIdToInteger,
            "grantAdvertId", this::extractSchemeIdFromAdvert,
            "applicationId", this::extractSchemeIdFromApplication,
            "createGrantAdvertDto", this::extractGrantSchemeIdFromDto,
            "applicationFormPostDTO", this::extractGrantSchemeIdFromDto,
            "applicationFormExistsDto", this::extractGrantSchemeIdFromDto
    );


    private Integer fetchSchemeIdFromMethodArgs(Map<String, Object> methodArgs) {
        for (String key : methodArgs.keySet()) {
            if (EXTRACTORS.containsKey(key)) {
                return EXTRACTORS.get(key).extractSchemeId(methodArgs.get(key));
            }
        }
        throw new IllegalArgumentException("Unable to retrieve scheme Id from request " + methodArgs.keySet());
    }

    public Integer extractSchemeIdFromAdvert(Object id) {

        if (id instanceof UUID) {
            GrantAdvert advert = grantAdvertRepository.findById((UUID) id).orElseThrow(NotFoundException::new);
            return advert.getScheme().getId();
        } else {
            throw new IllegalArgumentException("Unable to extract grantSchemeId from Advert: " +
                    "value is not an instance of UUID " + id);
        }
    }

    public Integer extractSchemeIdFromApplication(Object id) {

        if (id instanceof Integer) {
            ApplicationFormNoSections application = applicationFormRepository
                    .findByGrantApplicationId((Integer) id).orElseThrow(NotFoundException::new);
            return application.getGrantSchemeId();
        } else {
            throw new IllegalArgumentException("Unable to extract grantSchemeId from Application: " +
                    "value is not an instance of Integer " + id);
        }
    }

    public <T> Integer extractGrantSchemeIdFromDto(T dto) {
        try {
            Method method = dto.getClass().getMethod("getGrantSchemeId");
            return (Integer) method.invoke(dto);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Unable to extract grantSchemeId from DTO", e);
        }
    }

    private Integer castResponseIdToInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }

        // In some cases String integers are used in the request
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Value cannot be parsed to an integer: " + value);
        }
    }

    private boolean isStringInt(Object value) {
        try {
            Integer.parseInt((String) value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Map<String, Object> getMethodArguments(JoinPoint joinPoint) {
        Object[] methodArgs = joinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = methodSignature.getParameterNames();

        Map<String, Object> argumentsMap = new HashMap<>();
        int i = 0;
        for (String parameterName : parameterNames) {
            argumentsMap.put(parameterName, methodArgs[i++]);
        }
        return argumentsMap;
    }

}
