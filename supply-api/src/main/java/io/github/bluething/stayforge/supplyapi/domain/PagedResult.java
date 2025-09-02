package io.github.bluething.stayforge.supplyapi.domain;

import java.util.List;

public record PagedResult<T>(
        List<T> data,
        String cursor,
        Integer limit,
        Long total,
        Boolean hasNext,
        String nextCursor
) {}
