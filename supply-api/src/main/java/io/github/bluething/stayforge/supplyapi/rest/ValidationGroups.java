package io.github.bluething.stayforge.supplyapi.rest;

public interface ValidationGroups {
    /**
     * Validation group for create operations (POST)
     * - ID should not be provided
     * - All required fields must be present
     */
    interface Create {}

    /**
     * Validation group for update operations (PUT)
     * - ID must be provided and valid
     * - All fields are required (full replacement)
     */
    interface Update {}

    /**
     * Validation group for partial update operations (PATCH)
     * - ID must be provided and valid
     * - Only provided fields are validated
     */
    interface PartialUpdate {}
}
