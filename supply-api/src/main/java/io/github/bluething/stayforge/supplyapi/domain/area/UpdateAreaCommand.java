package io.github.bluething.stayforge.supplyapi.domain.area;

public record UpdateAreaCommand(
        String name,
        String slug
) {}
