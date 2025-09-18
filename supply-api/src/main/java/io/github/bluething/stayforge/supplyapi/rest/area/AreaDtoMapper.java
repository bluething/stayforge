package io.github.bluething.stayforge.supplyapi.rest.area;

import io.github.bluething.stayforge.supplyapi.domain.PagedResult;
import io.github.bluething.stayforge.supplyapi.domain.area.AreaData;
import io.github.bluething.stayforge.supplyapi.domain.area.AreaQuery;
import io.github.bluething.stayforge.supplyapi.domain.area.CreateAreaCommand;
import io.github.bluething.stayforge.supplyapi.domain.area.UpdateAreaCommand;
import io.github.bluething.stayforge.supplyapi.rest.PaginationMetadata;
import io.github.bluething.stayforge.supplyapi.rest.PaginationRequest;
import org.springframework.stereotype.Component;

@Component
class AreaDtoMapper {
    public CreateAreaCommand toCommand(CreateAreaRequest request) {
        return new CreateAreaCommand(
                request.name(),
                request.slug()
        );
    }
    public UpdateAreaCommand toCommand(UpdateAreaRequest request) {
        return new UpdateAreaCommand(
                request.name(),
                request.slug()
        );
    }
    public AreaResponse toResponse(AreaData areaData) {
        return new AreaResponse(
                areaData.id(),
                areaData.name(),
                areaData.slug()
        );
    }
    public AreaQuery toQuery(PaginationRequest pagination, String nameFilter) {
        return new AreaQuery(
                pagination.cursor(),
                pagination.limit(),
                nameFilter
        );
    }
    public AreaListResponse toListResponse(PagedResult<AreaData> pagedResult) {
        var areas = pagedResult.data().stream()
                .map(this::toResponse)
                .toList();

        var paginationMetadata = new PaginationMetadata(
                pagedResult.cursor(),
                pagedResult.limit(),
                pagedResult.total(),
                pagedResult.hasNext(),
                pagedResult.nextCursor()
        );

        return new AreaListResponse(areas, paginationMetadata);
    }
}
