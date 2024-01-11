package gov.cabinetoffice.gap.adminbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackDto {

    @NotNull(message = "A satisfaction score must be provided")
    @Positive(message = "A valid satisfaction score must be provided")
    private Integer satisfaction;

    // @Size(max = 1000, message = "Feedback comment cannot be longer than 1000
    // characters")
    private String comment;

}
