package com.athletics.apigateway.presentationlayer.competition;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.athletics.apigateway.domainclientlayer.competition.CompetitionStatusEnum;
import com.athletics.apigateway.domainclientlayer.competition.CompetitionResultEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CompetitionControllerIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    private MockRestServiceServer mockServer;

    private static final String BASE_URI                 = "/api/v1/teams";
    private static final String SERVICE_BASE_URI         = "http://localhost:7004/api/v1/teams";
    private static final String VALID_TEAM_ID            = "11111111-1111-1111-1111-111111111111";
    private static final String VALID_COMPETITION_ID     = "aaaaaaa1-1aaa-1aaa-1aaa-aaaaaaaaaaa1";
    private static final String NOT_FOUND_COMPETITION_ID = "aaaaaaa1-1aaa-1aaa-1aaa-aaaaaaaaaaa3";
    private static final String INVALID_UUID             = "bad-uuid";

    @BeforeEach
    void init() {
        // enable ISO‐8601 dates
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ensure RestTemplate uses the same mapper
        for (HttpMessageConverter<?> conv : restTemplate.getMessageConverters()) {
            if (conv instanceof MappingJackson2HttpMessageConverter mj) {
                mj.setObjectMapper(mapper);
            }
        }

        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    // --- GET ALL ---
    @Test
    void whenGetAllCompetitionsExists_thenReturnListOfCompetitions() throws Exception {
        CompetitionResponseModel resp = CompetitionResponseModel.builder()
                .competitionId(VALID_COMPETITION_ID)
                .competitionName("Spring Invitational")
                .competitionDate(LocalDate.of(2025, 6, 1))
                .competitionStatus(CompetitionStatusEnum.SCHEDULED.toString())
                .competitionResult(CompetitionResultEnum.DRAW.toString())
                .teamId(VALID_TEAM_ID)
                .build();

        mockServer.expect(once(),
                        requestTo(new URI(SERVICE_BASE_URI + "/" + VALID_TEAM_ID + "/competitions")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(new CompetitionResponseModel[] { resp }),
                        MediaType.APPLICATION_JSON));

        webClient.get()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CompetitionResponseModel.class)
                .value(list -> {
                    assertNotNull(list);
                    assertEquals(1, list.size());
                    assertEquals(VALID_COMPETITION_ID, list.get(0).getCompetitionId());
                });

        mockServer.verify();
    }

    @Test
    void whenGetAllCompetitionsWithInvalidTeamId_thenReturnUnprocessableEntity() {
        webClient.get()
                .uri(BASE_URI + "/" + INVALID_UUID + "/competitions")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid teamId: " + INVALID_UUID);
    }

    // --- GET BY ID ---
    @Test
    void whenGetCompetitionByIdExists_thenReturnCompetition() throws Exception {
        CompetitionResponseModel resp = CompetitionResponseModel.builder()
                .competitionId(VALID_COMPETITION_ID)
                .competitionName("Autumn Challenge")
                .competitionDate(LocalDate.of(2025, 9, 15))
                .competitionStatus(CompetitionStatusEnum.ONGOING.toString())
                .competitionResult(CompetitionResultEnum.WIN.toString())
                .teamId(VALID_TEAM_ID)
                .build();

        mockServer.expect(once(),
                        requestTo(new URI(SERVICE_BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + VALID_COMPETITION_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(resp), MediaType.APPLICATION_JSON));

        webClient.get()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + VALID_COMPETITION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CompetitionResponseModel.class)
                .value(body -> {
                    assertEquals("Autumn Challenge", body.getCompetitionName());
                    assertEquals(LocalDate.of(2025, 9, 15), body.getCompetitionDate());
                });

        mockServer.verify();
    }

    @Test
    void whenGetCompetitionByIdWithInvalidCompetitionId_thenReturnUnprocessableEntity() {
        webClient.get()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + INVALID_UUID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid competitionId: " + INVALID_UUID);
    }

    @Test
    void whenGetCompetitionByIdNotFoundByService_thenReturnNotFound() throws Exception {
        mockServer.expect(once(),
                        requestTo(new URI(SERVICE_BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + NOT_FOUND_COMPETITION_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.get()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + NOT_FOUND_COMPETITION_ID)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    // --- CREATE ---
    @Test
    void whenCreateCompetitionWithValidData_thenReturnCreatedCompetition() throws Exception {
        CompetitionRequestModel req = CompetitionRequestModel.builder()
                .competitionName("Winter Classic")
                .competitionDate(LocalDate.of(2025, 12, 5))
                .competitionStatus(CompetitionStatusEnum.SCHEDULED.toString())
                .competitionResult(CompetitionResultEnum.DRAW.toString())
                .sponsorId(null)
                .facilityId(null)
                .build();

        CompetitionResponseModel created = CompetitionResponseModel.builder()
                .competitionId(VALID_COMPETITION_ID)
                .competitionName("Winter Classic")
                .competitionDate(LocalDate.of(2025, 12, 5))
                .competitionStatus(CompetitionStatusEnum.SCHEDULED.toString())
                .competitionResult(CompetitionResultEnum.DRAW.toString())
                .teamId(VALID_TEAM_ID)
                .build();

        mockServer.expect(once(),
                        requestTo(new URI(SERVICE_BASE_URI + "/" + VALID_TEAM_ID + "/competitions")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(mapper.writeValueAsString(req)))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(created)));

        webClient.post()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CompetitionResponseModel.class)
                .value(body -> assertEquals(VALID_COMPETITION_ID, body.getCompetitionId()));

        mockServer.verify();
    }

    @Test
    void whenCreateCompetitionWithInvalidTeamId_thenReturnUnprocessableEntity() {
        CompetitionRequestModel req = CompetitionRequestModel.builder()
                .competitionName("Invalid Team Test")
                .competitionDate(LocalDate.now())
                .competitionStatus(CompetitionStatusEnum.SCHEDULED.toString())
                .competitionResult(CompetitionResultEnum.LOSS.toString())
                .build();

        webClient.post()
                .uri(BASE_URI + "/" + INVALID_UUID + "/competitions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid teamId: " + INVALID_UUID);
    }

    // --- UPDATE ---
    @Test
    void whenUpdateCompetitionWithValidData_thenReturnUpdatedCompetition() throws Exception {
        CompetitionRequestModel req = CompetitionRequestModel.builder()
                .competitionName("Championship Final")
                .competitionDate(LocalDate.of(2025, 11, 20))
                .competitionStatus(CompetitionStatusEnum.COMPLETED.toString())
                .competitionResult(CompetitionResultEnum.WIN.toString())
                .sponsorId(null)
                .facilityId(null)
                .build();

        mockServer.expect(once(),
                        requestTo(new URI(SERVICE_BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + VALID_COMPETITION_ID)))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().json(mapper.writeValueAsString(req)))
                .andRespond(withNoContent());

        CompetitionResponseModel updated = CompetitionResponseModel.builder()
                .competitionId(VALID_COMPETITION_ID)
                .competitionName("Championship Final")
                .competitionDate(LocalDate.of(2025, 11, 20))
                .competitionStatus(CompetitionStatusEnum.COMPLETED.toString())
                .competitionResult(CompetitionResultEnum.WIN.toString())
                .teamId(VALID_TEAM_ID)
                .build();

        mockServer.expect(once(),
                        requestTo(new URI(SERVICE_BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + VALID_COMPETITION_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(updated), MediaType.APPLICATION_JSON));

        webClient.put()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + VALID_COMPETITION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CompetitionResponseModel.class)
                .value(body -> assertEquals("Championship Final", body.getCompetitionName()));

        mockServer.verify();
    }

    @Test
    void whenUpdateCompetitionWithInvalidCompetitionId_thenReturnUnprocessableEntity() {
        CompetitionRequestModel req = CompetitionRequestModel.builder()
                .competitionName("Should Fail")
                .competitionDate(LocalDate.now())
                .competitionStatus(CompetitionStatusEnum.SCHEDULED.toString())
                .competitionResult(CompetitionResultEnum.LOSS.toString())
                .build();

        webClient.put()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + INVALID_UUID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid competitionId: " + INVALID_UUID);
    }

    @Test
    void whenUpdateCompetitionNotFound_thenReturnNotFound() throws Exception {
        CompetitionRequestModel req = CompetitionRequestModel.builder()
                .competitionName("Ghost Comp")
                .competitionDate(LocalDate.now())
                .competitionStatus(CompetitionStatusEnum.SCHEDULED.toString())
                .competitionResult(CompetitionResultEnum.LOSS.toString())
                .build();

        mockServer.expect(once(),
                        requestTo(new URI(SERVICE_BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + NOT_FOUND_COMPETITION_ID)))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.put()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + NOT_FOUND_COMPETITION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    // --- DELETE ---
    @Test
    void whenDeleteCompetitionWithValidData_thenReturnNoContent() throws Exception {
        mockServer.expect(once(),
                        requestTo(new URI(SERVICE_BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + VALID_COMPETITION_ID)))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        webClient.delete()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + VALID_COMPETITION_ID)
                .exchange()
                .expectStatus().isNoContent();

        mockServer.verify();
    }

    @Test
    void whenDeleteCompetitionWithInvalidCompetitionId_thenReturnUnprocessableEntity() {
        webClient.delete()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + INVALID_UUID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid competitionId: " + INVALID_UUID);
    }

    @Test
    void whenDeleteCompetitionNotFound_thenReturnNotFound() throws Exception {
        mockServer.expect(once(),
                        requestTo(new URI(SERVICE_BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + NOT_FOUND_COMPETITION_ID)))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.delete()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + NOT_FOUND_COMPETITION_ID)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }
}
