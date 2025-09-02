package io.github.bluething.stayforge.supplyapi.rest.area;

import io.github.bluething.stayforge.supplyapi.rest.PaginationMetadata;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Paginated list of areas")
record AreaListResponse(
        @Schema(description = "List of areas")
        java.util.List<AreaResponse> data,

        @Schema(description = "Pagination metadata")
        PaginationMetadata pagination
) {}
