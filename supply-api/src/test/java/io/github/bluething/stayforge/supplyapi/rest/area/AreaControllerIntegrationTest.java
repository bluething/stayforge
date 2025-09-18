package io.github.bluething.stayforge.supplyapi.rest.area;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class AreaControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateArea_WhenValidRequest() throws Exception {
        // Given
        CreateAreaRequest request = new CreateAreaRequest("Kuta Beach", "kuta-beach");

        // When & Then
        mockMvc.perform(post("/api/v1/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Kuta Beach")))
                .andExpect(jsonPath("$.slug", is("kuta-beach")));
    }

    @Test
    void shouldReturnBadRequest_WhenInvalidSlug() throws Exception {
        // Given - invalid slug with spaces and uppercase
        CreateAreaRequest request = new CreateAreaRequest("Kuta Beach", "KUTA BEACH");

        // When & Then
        mockMvc.perform(post("/api/v1/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type", is("https://api.stayforge.com/problems/validation-error")))
                .andExpect(jsonPath("$.title", is("Validation failed")))
                .andExpect(jsonPath("$.extensions.validation_errors", hasSize(greaterThan(0))));
    }

    @Test
    void shouldReturnConflict_WhenDuplicateSlug() throws Exception {
        // Given - create first area
        CreateAreaRequest firstRequest = new CreateAreaRequest("Kuta", "kuta-bali");
        mockMvc.perform(post("/api/v1/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // When - try to create area with same slug
        CreateAreaRequest duplicateRequest = new CreateAreaRequest("Kuta Beach", "kuta-bali");

        // Then
        mockMvc.perform(post("/api/v1/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type", is("https://api.stayforge.com/problems/duplicate-slug")))
                .andExpect(jsonPath("$.title", is("Slug already exists")));
    }

    @Test
    void shouldGetArea_WhenValidId() throws Exception {
        // Given - create area first
        CreateAreaRequest createRequest = new CreateAreaRequest("Seminyak", "seminyak-bali");
        String createResponse = mockMvc.perform(post("/api/v1/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AreaResponse createdArea = objectMapper.readValue(createResponse, AreaResponse.class);

        // When & Then
        mockMvc.perform(get("/api/v1/areas/{id}", createdArea.id()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(createdArea.id().intValue())))
                .andExpect(jsonPath("$.name", is("Seminyak")))
                .andExpect(jsonPath("$.slug", is("seminyak-bali")));
    }

    @Test
    void shouldReturnNotFound_WhenInvalidId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/areas/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type", is("https://api.stayforge.com/problems/area-not-found")))
                .andExpect(jsonPath("$.title", is("Area not found")));
    }

    @Test
    void shouldListAreas_WithPagination() throws Exception {
        // Given - create multiple areas
        mockMvc.perform(post("/api/v1/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAreaRequest("Ubud", "ubud-bali"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAreaRequest("Canggu", "canggu-bali"))))
                .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/api/v1/areas")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.pagination.limit", is(10)))
                .andExpect(jsonPath("$.pagination.total", greaterThanOrEqualTo(2)));
    }

    @Test
    void shouldFilterAreas_ByName() throws Exception {
        // Given - create areas with different names
        mockMvc.perform(post("/api/v1/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAreaRequest("Kuta Beach", "kuta-beach-area"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAreaRequest("Seminyak Square", "seminyak-square"))))
                .andExpect(status().isCreated());

        // When & Then - filter by "kuta"
        mockMvc.perform(get("/api/v1/areas")
                        .param("name", "kuta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", containsStringIgnoringCase("kuta")));
    }

    @Test
    void shouldDeleteArea_WhenValidId() throws Exception {
        // Given - create area
        CreateAreaRequest createRequest = new CreateAreaRequest("Jimbaran", "jimbaran-bay");
        String createResponse = mockMvc.perform(post("/api/v1/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AreaResponse createdArea = objectMapper.readValue(createResponse, AreaResponse.class);

        // When - delete area
        mockMvc.perform(delete("/api/v1/areas/{id}", createdArea.id()))
                .andExpect(status().isNoContent());

        // Then - verify area is soft deleted
        mockMvc.perform(get("/api/v1/areas/{id}", createdArea.id()))
                .andExpect(status().isNotFound());
    }
}