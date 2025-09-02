package io.github.bluething.stayforge.supplyapi.rest.area;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AreaController.class)
@DisplayName("AreaController API Contract Tests")
class AreaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /api/v1/areas - Create Area")
    class CreateAreaTests {

        @Test
        @DisplayName("Should accept valid create request and return 201")
        void shouldAcceptValidCreateRequest() throws Exception {
            CreateAreaRequest request = new CreateAreaRequest("Kuta", "kuta-bali");

            mockMvc.perform(post("/api/v1/areas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Should reject request with null name")
        void shouldRejectNullName() throws Exception {
            CreateAreaRequest request = new CreateAreaRequest(null, "kuta-bali");

            mockMvc.perform(post("/api/v1/areas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject request with empty name")
        void shouldRejectEmptyName() throws Exception {
            CreateAreaRequest request = new CreateAreaRequest("", "kuta-bali");

            mockMvc.perform(post("/api/v1/areas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject request with blank name")
        void shouldRejectBlankName() throws Exception {
            CreateAreaRequest request = new CreateAreaRequest("   ", "kuta-bali");

            mockMvc.perform(post("/api/v1/areas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject request with name exceeding max length")
        void shouldRejectLongName() throws Exception {
            String longName = "A".repeat(101); // Max length is 100
            CreateAreaRequest request = new CreateAreaRequest(longName, "kuta-bali");

            mockMvc.perform(post("/api/v1/areas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject request with name below minimum length")
        void shouldRejectShortName() throws Exception {
            CreateAreaRequest request = new CreateAreaRequest("A", "kuta-bali"); // Min length is 2

            mockMvc.perform(post("/api/v1/areas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject request with null slug")
        void shouldRejectNullSlug() throws Exception {
            CreateAreaRequest request = new CreateAreaRequest("Kuta", null);

            mockMvc.perform(post("/api/v1/areas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject request with empty slug")
        void shouldRejectEmptySlug() throws Exception {
            CreateAreaRequest request = new CreateAreaRequest("Kuta", "");

            mockMvc.perform(post("/api/v1/areas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject request with blank slug")
        void shouldRejectBlankSlug() throws Exception {
            CreateAreaRequest request = new CreateAreaRequest("Kuta", "   ");

            mockMvc.perform(post("/api/v1/areas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject request with slug exceeding max length")
        void shouldRejectLongSlug() throws Exception {
            String longSlug = "a".repeat(101); // Max length is 100
            CreateAreaRequest request = new CreateAreaRequest("Kuta", longSlug);

            mockMvc.perform(post("/api/v1/areas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject request with slug below minimum length")
        void shouldRejectShortSlug() throws Exception {
            CreateAreaRequest request = new CreateAreaRequest("Kuta", "a"); // Min length is 2

            mockMvc.perform(post("/api/v1/areas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject request with invalid slug format")
        void shouldRejectInvalidSlugFormat() throws Exception {
            CreateAreaRequest request = new CreateAreaRequest("Kuta", "Invalid Slug!"); // Assuming @ValidSlug rejects spaces and special chars

            mockMvc.perform(post("/api/v1/areas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject request with invalid content type")
        void shouldRejectInvalidContentType() throws Exception {
            mockMvc.perform(post("/api/v1/areas")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("invalid"))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("Should reject empty request body")
        void shouldRejectEmptyBody() throws Exception {
            mockMvc.perform(post("/api/v1/areas")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/areas/{id} - Get Area by ID")
    class GetAreaTests {

        @Test
        @DisplayName("Should accept valid positive ID")
        void shouldAcceptValidId() throws Exception {
            mockMvc.perform(get("/api/v1/areas/123"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Should reject zero ID")
        void shouldRejectZeroId() throws Exception {
            mockMvc.perform(get("/api/v1/areas/0"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject negative ID")
        void shouldRejectNegativeId() throws Exception {
            mockMvc.perform(get("/api/v1/areas/-1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject non-numeric ID")
        void shouldRejectNonNumericId() throws Exception {
            mockMvc.perform(get("/api/v1/areas/abc"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should accept large valid ID")
        void shouldAcceptLargeId() throws Exception {
            mockMvc.perform(get("/api/v1/areas/999999999"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/areas/{id} - Update Area")
    class UpdateAreaTests {

        @Test
        @DisplayName("Should accept valid update request")
        void shouldAcceptValidUpdateRequest() throws Exception {
            UpdateAreaRequest request = new UpdateAreaRequest("Updated Kuta", "");

            mockMvc.perform(put("/api/v1/areas/123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Should reject update with invalid ID")
        void shouldRejectInvalidId() throws Exception {
            UpdateAreaRequest request = new UpdateAreaRequest("Updated Kuta", "");

            mockMvc.perform(put("/api/v1/areas/-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject update with null name")
        void shouldRejectNullNameInUpdate() throws Exception {
            UpdateAreaRequest request = new UpdateAreaRequest(null, "");

            mockMvc.perform(put("/api/v1/areas/123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject update with empty name")
        void shouldRejectEmptyNameInUpdate() throws Exception {
            UpdateAreaRequest request = new UpdateAreaRequest("", "");

            mockMvc.perform(put("/api/v1/areas/123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/areas/{id} - Delete Area")
    class DeleteAreaTests {

        @Test
        @DisplayName("Should accept valid delete request")
        void shouldAcceptValidDeleteRequest() throws Exception {
            mockMvc.perform(delete("/api/v1/areas/123"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Should reject delete with zero ID")
        void shouldRejectZeroIdForDelete() throws Exception {
            mockMvc.perform(delete("/api/v1/areas/0"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject delete with negative ID")
        void shouldRejectNegativeIdForDelete() throws Exception {
            mockMvc.perform(delete("/api/v1/areas/-1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject delete with non-numeric ID")
        void shouldRejectNonNumericIdForDelete() throws Exception {
            mockMvc.perform(delete("/api/v1/areas/abc"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/areas - List Areas")
    class ListAreasTests {

        @Test
        @DisplayName("Should accept request without any parameters")
        void shouldAcceptRequestWithoutParams() throws Exception {
            mockMvc.perform(get("/api/v1/areas"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Should accept valid cursor parameter")
        void shouldAcceptValidCursor() throws Exception {
            mockMvc.perform(get("/api/v1/areas")
                            .param("cursor", "eyJpZCI6MTIzfQ=="))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should accept valid limit parameter")
        void shouldAcceptValidLimit() throws Exception {
            mockMvc.perform(get("/api/v1/areas")
                            .param("limit", "20"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should accept minimum limit value")
        void shouldAcceptMinLimit() throws Exception {
            mockMvc.perform(get("/api/v1/areas")
                            .param("limit", "1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should accept maximum limit value")
        void shouldAcceptMaxLimit() throws Exception {
            mockMvc.perform(get("/api/v1/areas")
                            .param("limit", "100"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should reject limit below minimum")
        void shouldRejectLimitBelowMin() throws Exception {
            mockMvc.perform(get("/api/v1/areas")
                            .param("limit", "0"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject negative limit")
        void shouldRejectNegativeLimit() throws Exception {
            mockMvc.perform(get("/api/v1/areas")
                            .param("limit", "-1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject limit above maximum")
        void shouldRejectLimitAboveMax() throws Exception {
            mockMvc.perform(get("/api/v1/areas")
                            .param("limit", "101"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject non-numeric limit")
        void shouldRejectNonNumericLimit() throws Exception {
            mockMvc.perform(get("/api/v1/areas")
                            .param("limit", "abc"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should accept name filter parameter")
        void shouldAcceptNameFilter() throws Exception {
            mockMvc.perform(get("/api/v1/areas")
                            .param("name", "kuta"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should accept all valid parameters together")
        void shouldAcceptAllValidParams() throws Exception {
            mockMvc.perform(get("/api/v1/areas")
                            .param("cursor", "eyJpZCI6MTIzfQ==")
                            .param("limit", "20")
                            .param("name", "kuta"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should accept empty name filter")
        void shouldAcceptEmptyNameFilter() throws Exception {
            mockMvc.perform(get("/api/v1/areas")
                            .param("name", ""))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should accept name with special characters")
        void shouldAcceptNameWithSpecialChars() throws Exception {
            mockMvc.perform(get("/api/v1/areas")
                            .param("name", "kuta-bali & seminyak"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should accept unicode characters in name")
        void shouldAcceptUnicodeInName() throws Exception {
            mockMvc.perform(get("/api/v1/areas")
                            .param("name", "北京"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("HTTP Method and Content Type Validation")
    class HttpMethodAndContentTypeTests {

        @Test
        @DisplayName("Should accept request to list endpoint with GET")
        void shouldAcceptGetToListEndpoint() throws Exception {
            mockMvc.perform(get("/api/v1/areas"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should reject POST request to get endpoint")
        void shouldRejectPostToGetEndpoint() throws Exception {
            mockMvc.perform(post("/api/v1/areas/123"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("Should reject PATCH request on any endpoint")
        void shouldRejectPatchRequest() throws Exception {
            mockMvc.perform(patch("/api/v1/areas/123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("Should handle OPTIONS request for CORS")
        void shouldHandleOptionsRequest() throws Exception {
            mockMvc.perform(options("/api/v1/areas"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Request Headers Validation")
    class RequestHeadersTests {

        @Test
        @DisplayName("Should accept request with Accept header")
        void shouldAcceptWithAcceptHeader() throws Exception {
            mockMvc.perform(get("/api/v1/areas/123")
                            .header("Accept", MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle request with unsupported Accept header gracefully")
        void shouldHandleUnsupportedAcceptHeader() throws Exception {
            mockMvc.perform(get("/api/v1/areas/123")
                            .header("Accept", MediaType.APPLICATION_XML_VALUE))
                    .andExpect(status().isNotAcceptable());
        }

        @Test
        @DisplayName("Should accept request with User-Agent header")
        void shouldAcceptWithUserAgentHeader() throws Exception {
            mockMvc.perform(get("/api/v1/areas/123")
                            .header("User-Agent", "Stayforge-Client/1.0"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Response Content Type and Headers")
    class ResponseValidationTests {

        @Test
        @DisplayName("Should return JSON content type for successful responses")
        void shouldReturnJsonContentType() throws Exception {
            mockMvc.perform(get("/api/v1/areas/123"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Should return empty body for DELETE requests")
        void shouldReturnEmptyBodyForDelete() throws Exception {
            mockMvc.perform(delete("/api/v1/areas/123"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("Should return JSON for validation errors")
        void shouldReturnJsonForValidationErrors() throws Exception {
            mockMvc.perform(get("/api/v1/areas/-1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Testing")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long cursor parameter")
        void shouldHandleLongCursor() throws Exception {
            String longCursor = "A".repeat(1000);
            mockMvc.perform(get("/api/v1/areas")
                            .param("cursor", longCursor))
                    .andExpect(status().isOk()); // or isBadRequest depending on validation
        }

        @Test
        @DisplayName("Should handle request with valid cursor parameter")
        void shouldHandleValidCursorParam() throws Exception {
            mockMvc.perform(get("/api/v1/areas")
                            .param("cursor", "eyJpZCI6MTIzfQ==")
                            .param("limit", "20")
                            .param("name", "kuta"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle very large ID values")
        void shouldHandleLargeIdValues() throws Exception {
            mockMvc.perform(get("/api/v1/areas/9223372036854775807")) // Long.MAX_VALUE
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle ID values beyond Long range")
        void shouldHandleIdBeyondLongRange() throws Exception {
            mockMvc.perform(get("/api/v1/areas/99999999999999999999"))
                    .andExpect(status().isBadRequest());
        }
    }
}