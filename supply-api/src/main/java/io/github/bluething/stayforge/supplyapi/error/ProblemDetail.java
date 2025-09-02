package io.github.bluething.stayforge.supplyapi.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "RFC 7807 Problem Details for HTTP APIs")
public record ProblemDetail(
        @Schema(description = "A URI reference that identifies the problem type",
                example = "https://api.stayforge.com/problems/validation-error")
        String type,

        @Schema(description = "A short, human-readable summary of the problem",
                example = "Validation failed")
        String title,

        @Schema(description = "The HTTP status code", example = "400")
        Integer status,

        @Schema(description = "A human-readable explanation specific to this occurrence",
                example = "The request body contains invalid data")
        String detail,

        @Schema(description = "A URI reference that identifies the specific occurrence",
                example = "/api/v1/areas")
        String instance,

        @Schema(description = "When the error occurred")
        OffsetDateTime timestamp,

        @Schema(description = "Additional details about the error")
        Map<String, Object> extensions
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String type;
        private String title;
        private Integer status;
        private String detail;
        private String instance;
        private OffsetDateTime timestamp = OffsetDateTime.now();
        private Map<String, Object> extensions;

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public Builder instance(String instance) {
            this.instance = instance;
            return this;
        }

        public Builder timestamp(OffsetDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder extensions(Map<String, Object> extensions) {
            this.extensions = extensions;
            return this;
        }

        public ProblemDetail build() {
            return new ProblemDetail(type, title, status, detail, instance, timestamp, extensions);
        }
    }
}
