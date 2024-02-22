package gov.cabinetoffice.gap.adminbackend.mappers;

import gov.cabinetoffice.gap.adminbackend.dtos.submission.LambdaSubmissionDefinition;
import gov.cabinetoffice.gap.adminbackend.dtos.submission.SubmissionDto;
import gov.cabinetoffice.gap.adminbackend.entities.Submission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubmissionMapper {

    @Mapping(source = "definition.sections", target = "sections")
    @Mapping(source = "scheme.id", target = "schemeId")
    @Mapping(source = "scheme.name", target = "schemeName")
    @Mapping(source = "scheme.version", target = "schemeVersion")
    LambdaSubmissionDefinition submissionToLambdaSubmissionDefinition(Submission submission);

    @Mapping(source = "id", target = "submissionId")
    @Mapping(source = "scheme.id", target = "schemeId")
    @Mapping(source = "scheme.name", target = "schemeName")
    @Mapping(source = "definition.sections", target = "sections")
    SubmissionDto submissionToSubmissionDto(Submission submission);
}
