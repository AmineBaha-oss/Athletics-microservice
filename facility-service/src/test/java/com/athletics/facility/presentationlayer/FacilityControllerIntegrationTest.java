package com.athletics.facility.presentationlayer;

import com.athletics.facility.dataaccesslayer.FacilityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;


import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Sql({"/data-h2.sql"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FacilityControllerIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private FacilityRepository facilityRepository;

    private final String BASE_URI = "/api/v1/facilities";

    private final String VALID_ID        = "fac11111-1111-1111-1111-111111111111";
    private final String ANOTHER_ID      = "fac22222-2222-2222-2222-222222222222";
    private final String NOT_FOUND_ID    = "facabcde-1234-1234-1234-1234567890ab";
    private final String INVALID_ID      = "bad-fac-id";
    private final String NEW_ID          = "facabcde-0000-0000-0000-000000000000";

    @Test
    public void whenGetAllFacilities_thenReturnList() {
        long expected = facilityRepository.count();

        webClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(FacilityResponseModel.class)
                .value(list -> assertEquals(expected, list.size()));
    }

    @Test
    public  void whenGetByIdValid_thenReturnFacility() {
        webClient.get()
                .uri(BASE_URI + "/" + VALID_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(FacilityResponseModel.class)
                .value(f -> {
                    assertEquals(VALID_ID, f.getFacilityId());
                    assertNotNull(f.getFacilityName());
                    assertNotNull(f.getCapacity());
                    assertNotNull(f.getLocation());
                });
    }

    @Test
    public void whenGetByIdInvalid_thenReturnUnprocessableEntity() {
        webClient.get()
                .uri(BASE_URI + "/" + INVALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid facilityId provided: " + INVALID_ID);
    }

    @Test
    public  void whenGetByIdNotFound_thenReturnNotFound() {
        webClient.get()
                .uri(BASE_URI + "/" + NOT_FOUND_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Facility not found with ID: " + NOT_FOUND_ID);
    }

    @Test
    public void whenCreateValid_thenReturnsCreated() {
        FacilityRequestModel req = new FacilityRequestModel(
                NEW_ID,
                "Test Arena",
                1000,
                "Test City"
        );

        webClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(FacilityResponseModel.class)
                .value(f -> {
                    assertEquals(req.getFacilityId(), f.getFacilityId());
                    assertEquals(req.getFacilityName(), f.getFacilityName());
                    assertEquals(req.getCapacity(), f.getCapacity());
                    assertEquals(req.getLocation(), f.getLocation());
                });
    }

    @Test
    public  void whenCreateDuplicate_thenReturnUnprocessableEntity() {
        FacilityRequestModel dup = new FacilityRequestModel(
                VALID_ID,
                "Dupe Arena",
                1000,
                "Nowhere"
        );

        webClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dup)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Facility with ID already exists: " + VALID_ID);
    }

    @Test
    public void whenCreateInsufficientCapacity_thenReturnUnprocessableEntity() {
        FacilityRequestModel small = new FacilityRequestModel(
                NEW_ID,
                "Tiny Arena",
                10,
                "Smallville"
        );

        webClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(small)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("The facility capacity is too low. A minimum capacity of 50 is required.");
    }

    @Test
    public void whenUpdateValid_thenReturnsCreated() {
        FacilityRequestModel update = new FacilityRequestModel(
                VALID_ID,
                "Updated Stadium",
                80000,
                "Updated City"
        );

        webClient.put()
                .uri(BASE_URI + "/" + VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FacilityResponseModel.class)
                .value(f -> {
                    assertEquals(VALID_ID, f.getFacilityId());
                    assertEquals("Updated Stadium", f.getFacilityName());
                    assertEquals(80000, f.getCapacity());
                    assertEquals("Updated City", f.getLocation());
                });
    }

    @Test
    public  void whenUpdateInvalidId_thenReturnUnprocessableEntity() {
        FacilityRequestModel any = new FacilityRequestModel(
                INVALID_ID,
                "X",
                100,
                "Y"
        );

        webClient.put()
                .uri(BASE_URI + "/" + INVALID_ID)
                .bodyValue(any)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid facilityId provided: " + INVALID_ID);
    }

    @Test
    public  void whenUpdateNotFound_thenReturnNotFound() {
        FacilityRequestModel any = new FacilityRequestModel(
                NOT_FOUND_ID,
                "Nobody Stadium",
                1000,
                "Nowhere"
        );

        webClient.put()
                .uri(BASE_URI + "/" + NOT_FOUND_ID)
                .bodyValue(any)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("The provided ID [" + NOT_FOUND_ID + "] does not match any facility.");
    }

    @Test
    public  void whenDeleteValid_thenReturnNoContent() {
        webClient.delete()
                .uri(BASE_URI + "/" + ANOTHER_ID)
                .exchange()
                .expectStatus().isNoContent();

        webClient.get()
                .uri(BASE_URI + "/" + ANOTHER_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public  void whenDeleteInvalidId_thenReturnUnprocessableEntity() {
        webClient.delete()
                .uri(BASE_URI + "/" + INVALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid facilityId provided: " + INVALID_ID);
    }

    @Test
    public  void whenDeleteNotFound_thenReturnNotFound() {
        webClient.delete()
                .uri(BASE_URI + "/" + NOT_FOUND_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Facility not found with ID: " + NOT_FOUND_ID);
    }

    @Test
    public void whenUpdateInsufficientCapacity_thenReturnUnprocessableEntity() {
        FacilityRequestModel tooSmall = new FacilityRequestModel(
                VALID_ID,
                "Tiny Update Arena",
                10,                   // below the 50 minimum
                "Nowhere City"
        );

        webClient.put()
                .uri(BASE_URI + "/" + VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tooSmall)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("The facility capacity is too low. A minimum capacity of 50 is required.");
    }
}