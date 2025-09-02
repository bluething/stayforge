package io.github.bluething.stayforge.supplyapi.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pagination information")
public record PaginationMetadata(
        @Schema(description = "Current page cursor", example = "eyJpZCI6MTIzfQ==")
        String cursor,

        @Schema(description = "Number of items per page", example = "20")
        Integer limit,

        @Schema(description = "Total number of items", example = "150")
        Long total,

        @Schema(description = "Whether there are more items", example = "true")
        Boolean hasNext,

        @Schema(description = "Cursor for the next page", example = "eyJpZCI6MTQzfQ==")
        String nextCursor
) {
}
