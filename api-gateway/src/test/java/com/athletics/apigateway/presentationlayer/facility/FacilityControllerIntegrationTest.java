package com.athletics.apigateway.presentationlayer.facility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FacilityControllerIntegrationTest {

    @Autowired WebTestClient webClient;
    @Autowired RestTemplate restTemplate;
    @Autowired ObjectMapper mapper;

    private MockRestServiceServer mockServer;

    private static final String BASE_URI         = "/api/v1/facilities";
    private static final String SERVICE_BASE_URI = "http://localhost:7003/api/v1/facilities";

    private static final String VALID_ID        = "fac11111-1111-1111-1111-111111111111";
    private static final String NOT_FOUND_ID    = "facabcde-1234-1234-1234-1234567890ab";
    private static final String INVALID_ID      = "bad-fac-id";

    @BeforeEach
    void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void whenGetAllFacilities_thenReturnList() throws Exception {
        FacilityResponseModel f1 = new FacilityResponseModel(VALID_ID, "Stadium1", 50000, "City1");
        FacilityResponseModel f2 = new FacilityResponseModel("fac22222-2222-2222-2222-222222222222", "Arena2", 20000, "City2");

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(new FacilityResponseModel[]{f1, f2}),
                        MediaType.APPLICATION_JSON));

        webClient.get().uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FacilityResponseModel.class)
                .value(list -> assertEquals(2, list.size()));

        mockServer.verify();
    }

    @Test
    void whenGetByIdValid_thenReturnFacility() throws Exception {
        FacilityResponseModel f = new FacilityResponseModel(VALID_ID, "Stadium1", 50000, "City1");

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + VALID_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(f),
                        MediaType.APPLICATION_JSON));

        webClient.get().uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FacilityResponseModel.class)
                .value(resp -> assertEquals("Stadium1", resp.getFacilityName()));

        mockServer.verify();
    }

    @Test
    void whenGetByIdInvalid_thenReturnUnprocessableEntity() throws Exception {
        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + INVALID_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid facilityId provided: " + INVALID_ID + "\"}"));

        webClient.get().uri(BASE_URI + "/" + INVALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid facilityId provided: " + INVALID_ID);

        mockServer.verify();
    }

    @Test
    void whenGetByIdNotFound_thenReturnNotFound() throws Exception {
        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + NOT_FOUND_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.get().uri(BASE_URI + "/" + NOT_FOUND_ID)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void whenCreateValid_thenReturnsCreated() throws Exception {
        FacilityRequestModel req = new FacilityRequestModel(VALID_ID, "NewArena", 30000, "NewCity");
        FacilityResponseModel created = new FacilityResponseModel(VALID_ID, "NewArena", 30000, "NewCity");

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(mapper.writeValueAsString(req)))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(created)));

        webClient.post().uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(FacilityResponseModel.class)
                .value(resp -> assertEquals("NewArena", resp.getFacilityName()));

        mockServer.verify();
    }

    @Test
    void whenCreateInvalid_thenReturnUnprocessableEntity() throws Exception {
        FacilityRequestModel req = new FacilityRequestModel(INVALID_ID, "", 10, "X");

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid facilityId provided: " + INVALID_ID + "\"}"));

        webClient.post().uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid facilityId provided: " + INVALID_ID);

        mockServer.verify();
    }

    @Test
    void whenUpdateValid_thenReturnsOk() throws Exception {
        FacilityRequestModel update = new FacilityRequestModel(VALID_ID, "UpdatedStadium", 60000, "UpdatedCity");
        FacilityResponseModel updated = new FacilityResponseModel(VALID_ID, "UpdatedStadium", 60000, "UpdatedCity");

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + VALID_ID))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().json(mapper.writeValueAsString(update)))
                .andRespond(withNoContent());
        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + VALID_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(updated), MediaType.APPLICATION_JSON));

        webClient.put().uri(BASE_URI + "/" + VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FacilityResponseModel.class)
                .value(resp -> assertEquals("UpdatedStadium", resp.getFacilityName()));

        mockServer.verify();
    }

    @Test
    void whenUpdateInvalid_thenReturnUnprocessableEntity() throws Exception {
        FacilityRequestModel any = new FacilityRequestModel(INVALID_ID, "X", 100, "Y");

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + INVALID_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid facilityId provided: " + INVALID_ID + "\"}"));

        webClient.put().uri(BASE_URI + "/" + INVALID_ID)
                .bodyValue(any)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid facilityId provided: " + INVALID_ID);

        mockServer.verify();
    }

    @Test
    void whenUpdateNotFound_thenReturnNotFound() throws Exception {
        FacilityRequestModel any = new FacilityRequestModel(NOT_FOUND_ID, "Ghost", 1000, "Nowhere");

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + NOT_FOUND_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.put().uri(BASE_URI + "/" + NOT_FOUND_ID)
                .bodyValue(any)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void whenDeleteValid_thenReturnNoContent() throws Exception {
        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + VALID_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        webClient.delete().uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isNoContent();

        mockServer.verify();
    }

    @Test
    void whenDeleteInvalid_thenReturnUnprocessableEntity() throws Exception {
        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + INVALID_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid facilityId provided: " + INVALID_ID + "\"}"));

        webClient.delete().uri(BASE_URI + "/" + INVALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid facilityId provided: " + INVALID_ID);

        mockServer.verify();
    }

    @Test
    void whenDeleteNotFound_thenReturnNotFound() throws Exception {
        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + NOT_FOUND_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.delete().uri(BASE_URI + "/" + NOT_FOUND_ID)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }
}
