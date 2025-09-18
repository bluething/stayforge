package io.github.bluething.stayforge.supplyapi.rest.area;

import io.github.bluething.stayforge.supplyapi.domain.area.AreaService;
import io.github.bluething.stayforge.supplyapi.rest.PaginationRequest;
import io.github.bluething.stayforge.supplyapi.rest.ValidationGroups;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/areas")
@Validated
@Tag(name = "Areas", description = "Geographic area management for hotel operators")
@RequiredArgsConstructor
class AreaController {

    private final AreaService areaService;
    private final AreaDtoMapper dtoMapper;

    @Operation(
            summary = "Create a new area",
            description = "Creates a new geographic area with a unique name and slug"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Area created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AreaResponse.class),
                            examples = @ExampleObject(
                                    name = "Created area",
                                    value = """
                        {
                            "id": 123,
                            "name": "Kuta",
                            "slug": "kuta-bali"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = io.github.bluething.stayforge.supplyapi.error.ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Area with the same slug already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = io.github.bluething.stayforge.supplyapi.error.ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Rate limit exceeded"
            )
    })
    @PostMapping
    public ResponseEntity<AreaResponse> createArea(
            @Validated(ValidationGroups.Create.class) @RequestBody CreateAreaRequest request) {

        var command = dtoMapper.toCommand(request);
        var areaData = areaService.createArea(command);
        var response = dtoMapper.toResponse(areaData);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get area by ID",
            description = "Retrieves a single area by its unique identifier"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Area found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AreaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid area ID"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Area not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<AreaResponse> getArea(
            @Parameter(description = "Area unique identifier", example = "123")
            @PathVariable("id") @Positive(message = "Area ID must be positive") Long id) {

        var areaData = areaService.getAreaById(id);
        var response = dtoMapper.toResponse(areaData);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update an existing area",
            description = "Updates an area's name and slug. All fields are required."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Area updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AreaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Area not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Area with the same slug already exists"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<AreaResponse> updateArea(
            @Parameter(description = "Area unique identifier", example = "123")
            @PathVariable("id") @Positive(message = "Area ID must be positive") Long id,
            @Validated(ValidationGroups.Update.class) @RequestBody UpdateAreaRequest request) {

        var command = dtoMapper.toCommand(request);
        var areaData = areaService.updateArea(id, command);
        var response = dtoMapper.toResponse(areaData);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete an area",
            description = "Soft deletes an area. Cannot delete areas with active hotels."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Area deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid area ID"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Area not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Cannot delete area with active hotels"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArea(
            @Parameter(description = "Area unique identifier", example = "123")
            @PathVariable("id") @Positive(message = "Area ID must be positive") Long id) {

        areaService.deleteArea(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "List areas",
            description = "Retrieves a paginated list of areas with optional name filtering"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Areas retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AreaListResponse.class),
                            examples = @ExampleObject(
                                    name = "Area list",
                                    value = """
                        {
                            "data": [
                                {
                                    "id": 123,
                                    "name": "Kuta",
                                    "slug": "kuta-bali"
                                },
                                {
                                    "id": 124,
                                    "name": "Seminyak", 
                                    "slug": "seminyak-bali"
                                }
                            ],
                            "pagination": {
                                "cursor": null,
                                "limit": 20,
                                "total": 2,
                                "hasNext": false,
                                "nextCursor": null
                            }
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination parameters"
            )
    })
    @GetMapping
    public ResponseEntity<AreaListResponse> listAreas(
            @Parameter(description = "Cursor for pagination", example = "eyJpZCI6MTIzfQ==")
            @RequestParam(value = "cursor", required = false) String cursor,

            @Parameter(description = "Number of items per page (1-100)", example = "20")
            @RequestParam(value = "limit", required = false)
            @Min(value = 1, message = "Limit must be at least 1")
            @Max(value = 100, message = "Limit cannot exceed 100")
            Integer limit,

            @Parameter(description = "Filter by area name (case-insensitive partial match)", example = "kuta")
            @RequestParam(value = "name", required = false) String name) {

        var pagination = PaginationRequest.of(cursor, limit);
        var query = dtoMapper.toQuery(pagination, name);
        var pagedResult = areaService.listAreas(query);
        var response = dtoMapper.toListResponse(pagedResult);

        return ResponseEntity.ok(response);
    }
}
