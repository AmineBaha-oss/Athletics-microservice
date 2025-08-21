package com.athletics.apigateway.presentationlayer.team;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.athletics.apigateway.domainclientlayer.team.AthleteCategoryEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AthleteControllerIntegrationTest {

    @Autowired WebTestClient webClient;
    @Autowired RestTemplate restTemplate;
    @Autowired ObjectMapper mapper;

    private MockRestServiceServer mockServer;

    private static final String TEAM_URI         = "/api/v1/teams";
    private static final String BASE_URI         = TEAM_URI + "/11111111-1111-1111-1111-111111111111/athletes";
    private static final String SERVICE_BASE_URI = "http://localhost:7001/api/v1/11111111-1111-1111-1111-111111111111/athletes";

    private static final String VALID_ATHLETE_ID     = "ath11111-1111-1111-1111-111111111111";
    private static final String NOT_FOUND_ATHLETE_ID = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
    private static final String INVALID_ID           = "bad-ath-id";

    @BeforeEach
    void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        for (HttpMessageConverter<?> conv : restTemplate.getMessageConverters()) {
            if (conv instanceof MappingJackson2HttpMessageConverter mj) {
                mj.setObjectMapper(mapper);
            }
        }

        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void whenGetAllAthletes_thenReturnList() throws Exception {
        AthleteResponseModel a1 = new AthleteResponseModel(
                VALID_ATHLETE_ID, "John", "Doe",
                LocalDate.of(2000, 1, 1), AthleteCategoryEnum.SENIOR);
        AthleteResponseModel a2 = new AthleteResponseModel(
                "ath22222-2222-2222-2222-222222222222",
                "Jane", "Roe",
                LocalDate.of(2002, 2, 2), AthleteCategoryEnum.JUNIOR);

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(new AthleteResponseModel[]{a1, a2}),
                        MediaType.APPLICATION_JSON));

        webClient.get().uri(BASE_URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AthleteResponseModel.class)
                .value(list -> assertEquals(2, list.size()));

        mockServer.verify();
    }

    @Test
    void whenGetByIdValid_thenReturnAthlete() throws Exception {
        AthleteResponseModel a = new AthleteResponseModel(
                VALID_ATHLETE_ID, "John", "Doe",
                LocalDate.of(2000, 1, 1), AthleteCategoryEnum.SENIOR);

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + VALID_ATHLETE_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(a),
                        MediaType.APPLICATION_JSON));

        webClient.get().uri(BASE_URI + "/" + VALID_ATHLETE_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AthleteResponseModel.class)
                .value(resp -> assertEquals("John", resp.getFirstName()));

        mockServer.verify();
    }

    @Test
    void whenGetByIdInvalid_thenReturnUnprocessableEntity() throws Exception {
        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + INVALID_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid athleteId provided: " + INVALID_ID + "\"}"));

        webClient.get().uri(BASE_URI + "/" + INVALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid athleteId provided: " + INVALID_ID);

        mockServer.verify();
    }

    @Test
    void whenGetByIdNotFound_thenReturnNotFound() throws Exception {
        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + NOT_FOUND_ATHLETE_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.get().uri(BASE_URI + "/" + NOT_FOUND_ATHLETE_ID)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void whenCreateValid_thenReturnsCreated() throws Exception {
        AthleteRequestModel req = new AthleteRequestModel(
                "Alice", "Smith",
                LocalDate.of(2001, 1, 1),
                AthleteCategoryEnum.JUNIOR);

        AthleteResponseModel created = new AthleteResponseModel(
                "new-id", "Alice", "Smith",
                LocalDate.of(2001, 1, 1),
                AthleteCategoryEnum.JUNIOR);

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI))
                .andExpect(method(HttpMethod.POST))
                // now dateOfBirth will be serialized as ISO string
                .andExpect(jsonPath("$.dateOfBirth").value("2001-01-01"))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(created)));

        webClient.post().uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AthleteResponseModel.class)
                .value(resp -> assertEquals("Alice", resp.getFirstName()));

        mockServer.verify();
    }

    @Test
    void whenCreateInvalid_thenReturnUnprocessableEntity() throws Exception {
        AthleteRequestModel req = new AthleteRequestModel(
                "", "", LocalDate.now(), AthleteCategoryEnum.SENIOR);

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid athlete data\"}"));

        webClient.post().uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid athlete data");

        mockServer.verify();
    }

    @Test
    void whenUpdateValid_thenReturnsOk() throws Exception {
        AthleteRequestModel update = new AthleteRequestModel(
                "John", "Doe Jr.",
                LocalDate.of(2000, 1, 1),
                AthleteCategoryEnum.SENIOR);
        AthleteResponseModel updated = new AthleteResponseModel(
                VALID_ATHLETE_ID, "John", "Doe Jr.",
                LocalDate.of(2000, 1, 1),
                AthleteCategoryEnum.SENIOR);

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + VALID_ATHLETE_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withNoContent());
        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + VALID_ATHLETE_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(updated),
                        MediaType.APPLICATION_JSON));

        webClient.put().uri(BASE_URI + "/" + VALID_ATHLETE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AthleteResponseModel.class)
                .value(resp -> assertEquals("Doe Jr.", resp.getLastName()));

        mockServer.verify();
    }

    @Test
    void whenUpdateInvalid_thenReturnUnprocessableEntity() throws Exception {
        AthleteRequestModel any = new AthleteRequestModel(
                "X", "Y", LocalDate.now(), AthleteCategoryEnum.JUNIOR);

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + INVALID_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid athleteId provided: " + INVALID_ID + "\"}"));

        webClient.put().uri(BASE_URI + "/" + INVALID_ID)
                .bodyValue(any)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid athleteId provided: " + INVALID_ID);

        mockServer.verify();
    }

    @Test
    void whenUpdateNotFound_thenReturnNotFound() throws Exception {
        AthleteRequestModel any = new AthleteRequestModel(
                "X", "Y", LocalDate.now(), AthleteCategoryEnum.JUNIOR);

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + NOT_FOUND_ATHLETE_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.put().uri(BASE_URI + "/" + NOT_FOUND_ATHLETE_ID)
                .bodyValue(any)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void whenDeleteValid_thenReturnNoContent() throws Exception {
        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + VALID_ATHLETE_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        webClient.delete().uri(BASE_URI + "/" + VALID_ATHLETE_ID)
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
                        .body("{\"message\":\"Invalid athleteId provided: " + INVALID_ID + "\"}"));

        webClient.delete().uri(BASE_URI + "/" + INVALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid athleteId provided: " + INVALID_ID);

        mockServer.verify();
    }
}
