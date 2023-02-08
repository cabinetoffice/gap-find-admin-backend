package gov.cabinetoffice.gap.adminbackend.controllers.pages;

import gov.cabinetoffice.gap.adminbackend.dtos.grantadvert.GetGrantAdvertPageResponseDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.pages.AdvertPreviewPageDto;
import gov.cabinetoffice.gap.adminbackend.dtos.pages.AdvertSectionOverviewPageDTO;
import gov.cabinetoffice.gap.adminbackend.dtos.pages.AdvertSummaryPageDTO;
import gov.cabinetoffice.gap.adminbackend.services.GrantAdvertService;
import gov.cabinetoffice.gap.adminbackend.services.pages.PagesAdvertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/pages/adverts")
@Tag(name = "Advert Builder Pages", description = "API for getting advert builder page data")
@RequiredArgsConstructor
public class PagesAdvertController {

    private final GrantAdvertService grantAdvertService;

    private final PagesAdvertService pagesAdvertService;

    @GetMapping("/section-overview")
    @Operation(summary = "Retrieve the section-overview page content.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all the page content.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdvertSectionOverviewPageDTO.class))),
            @ApiResponse(responseCode = "404", description = "No scheme or advert found with matching id.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "You do not have permissions to access the scheme needed.",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity<AdvertSectionOverviewPageDTO> getSectionOverviewContent(
            @RequestParam @NotNull final String schemeId, @RequestParam @NotNull final UUID advertId) {
        log.info("Creating page content for section-overview");
        final AdvertSectionOverviewPageDTO response = pagesAdvertService.buildSectionOverviewPageContent(schemeId,
                advertId);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{grantAdvertId}/questions-page")
    @Operation(summary = "Get view data to construct advert builder question response pages")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned page data for definition page requested",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetGrantAdvertPageResponseDTO.class))),
            @ApiResponse(responseCode = "400",
                    description = "Required path variables or request params not provided in expected format",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "You dont have permissions to access this advert",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Unable to find grant advert with id provided",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity<GetGrantAdvertPageResponseDTO> getQuestionsResponsePage(@PathVariable UUID grantAdvertId,
            @RequestParam String advertSectionId, @RequestParam String advertPageId) {

        GetGrantAdvertPageResponseDTO pageResponse = grantAdvertService.getAdvertBuilderPageData(grantAdvertId,
                advertSectionId, advertPageId);

        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping("/summary")
    @Operation(summary = "Retrieve the advert summary page content.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all the page content.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdvertSectionOverviewPageDTO.class))),
            @ApiResponse(responseCode = "404", description = "No scheme or advert found with matching id.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "You do not have permissions to access the scheme needed.",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity<AdvertSummaryPageDTO> getSummaryContent(@RequestParam @NotNull final String schemeId,
            @RequestParam @NotNull final UUID advertId) {
        log.info("Creating page content for advert summary");
        final AdvertSummaryPageDTO response = pagesAdvertService.buildSummaryPageContent(schemeId, advertId);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{grantAdvertId}/preview")
    @Operation(summary = "Get advert data to construct advert preview page")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returned advert data for preview page",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdvertPreviewPageDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Required path variables or request params not provided in expected format",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions to access this advert",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Unable to find grant advert with id provided",
                    content = @Content(mediaType = "application/json")) })
    public ResponseEntity<AdvertPreviewPageDto> getAdvertPreview(@PathVariable UUID grantAdvertId) {
        AdvertPreviewPageDto advertPreviewPageDto = pagesAdvertService.buildAdvertPreview(grantAdvertId);
        return ResponseEntity.ok(advertPreviewPageDto);
    }

}
