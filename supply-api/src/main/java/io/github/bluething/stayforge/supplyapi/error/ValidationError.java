package io.github.bluething.stayforge.supplyapi.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Validation error details")
public record ValidationError(
        @Schema(description = "Field path", example = "name")
        String field,

        @Schema(description = "Rejected value", example = "")
        Object rejectedValue,

        @Schema(description = "Error message", example = "must not be blank")
        String message
) {}
