package com.athletics.apigateway.presentationlayer.team;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.athletics.apigateway.domainclientlayer.team.TeamLevelEnum;
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
class TeamControllerIntegrationTest {

    @Autowired WebTestClient webClient;
    @Autowired RestTemplate restTemplate;
    @Autowired ObjectMapper mapper;

    private MockRestServiceServer mockServer;

    private static final String BASE_URI         = "/api/v1/teams";
    private static final String SERVICE_BASE_URI = "http://localhost:7001/api/v1/teams";

    private static final String VALID_ID        = "11111111-1111-1111-1111-111111111111";
    private static final String NOT_FOUND_ID    = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    private static final String INVALID_ID      = "bad-team-id";

    @BeforeEach
    void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void whenGetAllTeams_thenReturnList() throws Exception {
        TeamResponseModel t1 = new TeamResponseModel(VALID_ID, "Eagles", "Coach A", TeamLevelEnum.COLLEGE);
        TeamResponseModel t2 = new TeamResponseModel("22222222-2222-2222-2222-222222222222", "Hawks", "Coach B", TeamLevelEnum.PROFESSIONAL);

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new TeamResponseModel[]{t1,t2}),
                        MediaType.APPLICATION_JSON));

        webClient.get().uri(BASE_URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TeamResponseModel.class)
                .value(list -> assertEquals(2, list.size()));

        mockServer.verify();
    }

    @Test
    void whenGetByIdValid_thenReturnTeam() throws Exception {
        TeamResponseModel t = new TeamResponseModel(VALID_ID, "Eagles", "Coach A", TeamLevelEnum.COLLEGE);
        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + VALID_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(t), MediaType.APPLICATION_JSON));

        webClient.get().uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeamResponseModel.class)
                .value(resp -> assertEquals("Eagles", resp.getTeamName()));

        mockServer.verify();
    }

    @Test
    void whenGetByIdInvalid_thenReturnUnprocessableEntity() throws Exception {
        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + INVALID_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid teamId provided: " + INVALID_ID + "\"}"));

        webClient.get().uri(BASE_URI + "/" + INVALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid teamId provided: " + INVALID_ID);

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
        TeamRequestModel req = new TeamRequestModel(VALID_ID, "NewTeam", "Coach X", TeamLevelEnum.NATIONAL);
        TeamResponseModel created = new TeamResponseModel(VALID_ID, "NewTeam", "Coach X", TeamLevelEnum.NATIONAL);

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
                .expectBody(TeamResponseModel.class)
                .value(resp -> assertEquals("NewTeam", resp.getTeamName()));

        mockServer.verify();
    }

    @Test
    void whenCreateInvalid_thenReturnUnprocessableEntity() throws Exception {
        TeamRequestModel req = new TeamRequestModel(INVALID_ID, "", "", TeamLevelEnum.COLLEGE);

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid teamId provided: " + INVALID_ID + "\"}"));

        webClient.post().uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid teamId provided: " + INVALID_ID);

        mockServer.verify();
    }

    @Test
    void whenUpdateValid_thenReturnsOk() throws Exception {
        TeamRequestModel update = new TeamRequestModel(VALID_ID, "UpdatedTeam", "Coach Y", TeamLevelEnum.NATIONAL);
        TeamResponseModel updated = new TeamResponseModel(VALID_ID, "UpdatedTeam", "Coach Y", TeamLevelEnum.NATIONAL);

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + VALID_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withNoContent());
        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + VALID_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(updated), MediaType.APPLICATION_JSON));

        webClient.put().uri(BASE_URI + "/" + VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TeamResponseModel.class)
                .value(resp -> assertEquals("UpdatedTeam", resp.getTeamName()));

        mockServer.verify();
    }

    @Test
    void whenUpdateInvalid_thenReturnUnprocessableEntity() throws Exception {
        TeamRequestModel any = new TeamRequestModel(INVALID_ID, "X", "Y", TeamLevelEnum.HIGH_SCHOOL);

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + INVALID_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid teamId provided: " + INVALID_ID + "\"}"));

        webClient.put().uri(BASE_URI + "/" + INVALID_ID)
                .bodyValue(any)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid teamId provided: " + INVALID_ID);

        mockServer.verify();
    }

    @Test
    void whenUpdateNotFound_thenReturnNotFound() throws Exception {
        TeamRequestModel any = new TeamRequestModel(NOT_FOUND_ID, "Ghost", "NoOne", TeamLevelEnum.COLLEGE);

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
                        .body("{\"message\":\"Invalid teamId provided: " + INVALID_ID + "\"}"));

        webClient.delete().uri(BASE_URI + "/" + INVALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid teamId provided: " + INVALID_ID);

        mockServer.verify();
    }
}
