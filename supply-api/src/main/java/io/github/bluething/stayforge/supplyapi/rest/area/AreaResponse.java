package io.github.bluething.stayforge.supplyapi.rest.area;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Area details")
record AreaResponse(
        @Schema(description = "Unique area identifier", example = "123")
        Long id,

        @Schema(description = "Area name", example = "Kuta")
        String name,

        @Schema(description = "URL-friendly identifier", example = "kuta-bali")
        String slug
) {}
