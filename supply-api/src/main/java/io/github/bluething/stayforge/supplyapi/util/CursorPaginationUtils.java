package io.github.bluething.stayforge.supplyapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CursorPaginationUtils {
    private final ObjectMapper objectMapper;

    /**
     * Encode cursor from ID
     */
    public String encodeCursor(Long id) {
        if (id == null) {
            return null;
        }

        try {
            CursorData cursorData = new CursorData(id);
            String json = objectMapper.writeValueAsString(cursorData);
            return Base64.getUrlEncoder().encodeToString(json.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode cursor", e);
        }
    }

    /**
     * Decode cursor to ID
     */
    public Optional<Long> decodeCursor(String cursor) {
        if (cursor == null || cursor.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cursor);
            CursorData cursorData = objectMapper.readValue(decoded, CursorData.class);
            return Optional.of(cursorData.id());
        } catch (Exception e) {
            return Optional.empty(); // Invalid cursor, start from beginning
        }
    }
}
