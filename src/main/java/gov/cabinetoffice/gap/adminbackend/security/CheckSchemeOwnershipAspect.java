package gov.cabinetoffice.gap.adminbackend.security;

import gov.cabinetoffice.gap.adminbackend.entities.ApplicationFormEntity;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        SchemeEntity scheme = schemeRepository.findById(schemeId).orElseThrow(NotFoundException::new);

        boolean isSchemeEditor = scheme.getGrantAdmins().stream().anyMatch(grantAdmin -> grantAdmin.getId()
                .equals(session.getGrantAdminId()));

        if (!isSchemeEditor) {
            throw new AccessDeniedException(String.format("User with id %s does not have access to scheme with id %s",
                    session.getGrantAdminId(), schemeId));
        }
    }

    private interface SchemeIdExtractor {
        Integer extractSchemeId(Object obj);
    }

    private final Map<String, SchemeIdExtractor> schemeIdExtractors = Map.of(
            "schemeId", this::castResponseIdToInteger,
            "grantSchemeId", this::castResponseIdToInteger,
            "grantAdvertId", this::extractSchemeIdFromAdvert,
            "applicationId", this::extractSchemeIdFromApplication,
            "createGrantAdvertDto", this::extractGrantSchemeIdFromDto,
            "applicationFormPostDTO", this::extractGrantSchemeIdFromDto,
            "applicationFormExistsDTO", this::extractGrantSchemeIdFromDto
    );


    private Integer fetchSchemeIdFromMethodArgs(Map<String, Object> methodArgs) {
        for (Map.Entry<String,Object> entry : methodArgs.entrySet()) {
            if (schemeIdExtractors.containsKey(entry.getKey())) {
                return schemeIdExtractors.get(entry.getKey()).extractSchemeId(methodArgs.get(entry.getKey()));
            }
        }
        throw new IllegalArgumentException("Unable to retrieve scheme Id from request " + methodArgs.keySet());
    }

    public Integer extractSchemeIdFromAdvert(Object value) {

        if (value instanceof UUID id) {
            GrantAdvert advert = grantAdvertRepository.findById(id).orElseThrow(NotFoundException::new);
            return advert.getScheme().getId();
        } else {
            throw new IllegalArgumentException("Unable to extract grantSchemeId from Advert: " +
                    "value is not an instance of UUID " + value);
        }
    }

    public Integer extractSchemeIdFromApplication(Object value) {

        if (value instanceof Integer id) {
            ApplicationFormEntity application = applicationFormRepository
                    .findById(id).orElseThrow(NotFoundException::new);
            return application.getGrantSchemeId();
        } else {
            throw new IllegalArgumentException("Unable to extract grantSchemeId from Application: " +
                    "value is not an instance of Integer " + value);
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
        if (value instanceof Integer id) {
            return id;
        }

        // In some cases String integers are used in the request
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Value cannot be parsed to an integer: " + value);
        }
    }

    private Map<String, Object> getMethodArguments(JoinPoint joinPoint) {
        Object[] methodArgs = joinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = methodSignature.getParameterNames();

        return IntStream.range(0, parameterNames.length)
                .boxed()
                .collect(Collectors.toMap(i -> parameterNames[i], i -> methodArgs[i]));
    }

}
