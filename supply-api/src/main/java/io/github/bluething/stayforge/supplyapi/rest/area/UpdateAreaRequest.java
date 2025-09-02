package io.github.bluething.stayforge.supplyapi.rest.area;

import io.github.bluething.stayforge.supplyapi.rest.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update an existing area")
record UpdateAreaRequest(
        @Schema(description = "Area name", example = "Kuta Beach", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Name is required", groups = ValidationGroups.Update.class)
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters", groups = ValidationGroups.Update.class)
        String name,

        @Schema(description = "URL-friendly identifier", example = "kuta-beach-bali", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Slug is required", groups = ValidationGroups.Update.class)
        @ValidSlug(groups = ValidationGroups.Update.class)
        @Size(min = 2, max = 100, message = "Slug must be between 2 and 100 characters", groups = ValidationGroups.Update.class)
        String slug
) {}
