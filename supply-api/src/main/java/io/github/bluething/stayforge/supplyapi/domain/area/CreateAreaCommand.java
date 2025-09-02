package io.github.bluething.stayforge.supplyapi.domain.area;

public record CreateAreaCommand(
        String name,
        String slug
) {}
