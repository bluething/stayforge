package io.github.bluething.stayforge.supplyapi.domain.area;

public record AreaQuery(String cursor,
                        Integer limit,
                        String nameFilter) {
}
