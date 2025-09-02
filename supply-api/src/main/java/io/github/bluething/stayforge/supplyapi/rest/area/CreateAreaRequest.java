package io.github.bluething.stayforge.supplyapi.rest.area;

import io.github.bluething.stayforge.supplyapi.rest.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new area")
record CreateAreaRequest(
        @Schema(description = "Area name", example = "Kuta", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Name is required", groups = ValidationGroups.Create.class)
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters", groups = ValidationGroups.Create.class)
        String name,

        @Schema(description = "URL-friendly identifier", example = "kuta-bali", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Slug is required", groups = ValidationGroups.Create.class)
        @ValidSlug(groups = ValidationGroups.Create.class)
        @Size(min = 2, max = 100, message = "Slug must be between 2 and 100 characters", groups = ValidationGroups.Create.class)
        String slug
) {}
