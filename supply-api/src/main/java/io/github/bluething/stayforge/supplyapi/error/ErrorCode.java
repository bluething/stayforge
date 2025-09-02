package io.github.bluething.stayforge.supplyapi.error;

public enum ErrorCode {
    // Validation errors
    VALIDATION_ERROR("validation-error", "Validation failed"),
    INVALID_REQUEST_FORMAT("invalid-request-format", "Invalid request format"),

    // Business logic errors
    AREA_NOT_FOUND("area-not-found", "Area not found"),
    HOTEL_NOT_FOUND("hotel-not-found", "Hotel not found"),
    AREA_HAS_ACTIVE_HOTELS("area-has-active-hotels", "Cannot delete area with active hotels"),
    DUPLICATE_SLUG("duplicate-slug", "Slug already exists"),

    // System errors
    INTERNAL_SERVER_ERROR("internal-server-error", "Internal server error"),
    RATE_LIMIT_EXCEEDED("rate-limit-exceeded", "Rate limit exceeded");

    private final String code;
    private final String title;

    ErrorCode(String code, String title) {
        this.code = code;
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getTypeUri() {
        return "https://api.stayforge.com/problems/" + code;
    }
}
