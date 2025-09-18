package io.github.bluething.stayforge.supplyapi.domain.area;

import io.github.bluething.stayforge.supplyapi.domain.PagedResult;
import io.github.bluething.stayforge.supplyapi.error.BusinessException;
import io.github.bluething.stayforge.supplyapi.error.ErrorCode;
import io.github.bluething.stayforge.supplyapi.persistence.jooq.Tables;
import io.github.bluething.stayforge.supplyapi.persistence.jooq.tables.records.AreaRecord;
import io.github.bluething.stayforge.supplyapi.util.CursorPaginationUtils;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AreaService {
    private final DSLContext dsl;
    private final CursorPaginationUtils paginationUtils;

    /**
     * Create a new area
     */
    public AreaData createArea(CreateAreaCommand command) {
        // Check for duplicate slug
        boolean slugExists = dsl.fetchExists(
                dsl.selectFrom(Tables.AREA)
                        .where(Tables.AREA.SLUG.eq(command.slug()))
                        .and(Tables.AREA.DELETED_AT.isNull())
        );

        if (slugExists) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_SLUG,
                    HttpStatus.CONFLICT,
                    "Area with slug '" + command.slug() + "' already exists"
            );
        }

        AreaRecord record = dsl.newRecord(Tables.AREA);
        record.setName(command.name());
        record.setSlug(command.slug());
        record.store();

        return mapToAreaData(record);
    }

    /**
     * Get area by ID
     */
    @Transactional(readOnly = true)
    public AreaData getAreaById(Long id) {
        return dsl.select()
                .from(Tables.AREA)
                .where(Tables.AREA.ID.eq(id))
                .and(Tables.AREA.DELETED_AT.isNull())
                .fetchOptional()
                .map(this::mapToAreaData)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.AREA_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Area with ID " + id + " not found"
                ));
    }

    /**
     * Update an existing area
     */
    public AreaData updateArea(Long id, UpdateAreaCommand command) {
        AreaRecord record = dsl.selectFrom(Tables.AREA)
                .where(Tables.AREA.ID.eq(id))
                .and(Tables.AREA.DELETED_AT.isNull())
                .fetchOptional()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.AREA_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Area with ID " + id + " not found"
                ));

        // Check for duplicate slug (excluding current record)
        boolean slugExists = dsl.fetchExists(
                dsl.selectFrom(Tables.AREA)
                        .where(Tables.AREA.SLUG.eq(command.slug()))
                        .and(Tables.AREA.DELETED_AT.isNull())
                        .and(Tables.AREA.ID.ne(id))
        );

        if (slugExists) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_SLUG,
                    HttpStatus.CONFLICT,
                    "Area with slug '" + command.slug() + "' already exists"
            );
        }

        record.setName(command.name());
        record.setSlug(command.slug());
        record.store();

        return mapToAreaData(record);
    }

    /**
     * Soft delete an area
     */
    public void deleteArea(Long id) {
        // Check if area exists
        AreaRecord record = dsl.selectFrom(Tables.AREA)
                .where(Tables.AREA.ID.eq(id))
                .and(Tables.AREA.DELETED_AT.isNull())
                .fetchOptional()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.AREA_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Area with ID " + id + " not found"
                ));

        // Check if area has active hotels
        boolean hasActiveHotels = dsl.fetchExists(
                dsl.selectFrom(Tables.HOTEL)
                        .where(Tables.HOTEL.AREA_ID.eq(id))
                        .and(Tables.HOTEL.DELETED_AT.isNull())
        );

        if (hasActiveHotels) {
            throw new BusinessException(
                    ErrorCode.AREA_HAS_ACTIVE_HOTELS,
                    HttpStatus.CONFLICT,
                    "Cannot delete area with active hotels"
            );
        }

        record.setDeletedAt(OffsetDateTime.now());
        record.store();
    }

    /**
     * List areas with pagination and filtering
     */
    @Transactional(readOnly = true)
    public PagedResult<AreaData> listAreas(AreaQuery query) {
        var sqlQuery = dsl.select()
                .from(Tables.AREA)
                .where(Tables.AREA.DELETED_AT.isNull());

        // Apply name filter if provided
        if (query.nameFilter() != null && !query.nameFilter().trim().isEmpty()) {
            sqlQuery = sqlQuery.and(Tables.AREA.NAME.containsIgnoreCase(query.nameFilter().trim()));
        }

        // Apply cursor pagination
        Optional<Long> cursorId = paginationUtils.decodeCursor(query.cursor());
        if (cursorId.isPresent()) {
            sqlQuery = sqlQuery.and(Tables.AREA.ID.gt(cursorId.get()));
        }

        // Get one extra record to determine if there's a next page
        int limit = query.limit() != null ? Math.min(Math.max(query.limit(), 1), 100) : 20;
        Result<Record> records = sqlQuery
                .orderBy(Tables.AREA.ID.asc())
                .limit(limit + 1)
                .fetch();

        // Count total (for metadata)
        var countQuery = dsl.selectCount()
                .from(Tables.AREA)
                .where(Tables.AREA.DELETED_AT.isNull());

        if (query.nameFilter() != null && !query.nameFilter().trim().isEmpty()) {
            countQuery = countQuery.and(Tables.AREA.NAME.containsIgnoreCase(query.nameFilter().trim()));
        }

        long total = countQuery.fetchOne(0, Long.class);

        // Process results
        boolean hasNext = records.size() > limit;
        List<AreaData> areas = records.stream()
                .limit(limit)
                .map(this::mapToAreaData)
                .toList();

        String nextCursor = null;
        if (hasNext && !areas.isEmpty()) {
            nextCursor = paginationUtils.encodeCursor(areas.get(areas.size() - 1).id());
        }

        return new PagedResult<>(
                areas,
                query.cursor(),
                limit,
                total,
                hasNext,
                nextCursor
        );
    }

    private AreaData mapToAreaData(Record record) {
        return new AreaData(
                record.get(Tables.AREA.ID),
                record.get(Tables.AREA.NAME),
                record.get(Tables.AREA.SLUG)
        );
    }

    private AreaData mapToAreaData(AreaRecord record) {
        return new AreaData(
                record.getId(),
                record.getName(),
                record.getSlug()
        );
    }
}
