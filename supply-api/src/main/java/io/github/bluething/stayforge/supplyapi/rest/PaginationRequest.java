package io.github.bluething.stayforge.supplyapi.rest;

public record PaginationRequest(
        String cursor,
        Integer limit
) {

    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 100;

    /**
     * Create pagination request with defaults and validation
     */
    public static PaginationRequest of(String cursor, Integer limit) {
        int validatedLimit = limit == null ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        return new PaginationRequest(cursor, validatedLimit);
    }

    public int getValidatedLimit() {
        return Math.min(Math.max(limit == null ? DEFAULT_LIMIT : limit, 1), MAX_LIMIT);
    }
}
