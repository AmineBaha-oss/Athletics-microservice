package com.athletics.team.presentationlayer.Team;

import com.athletics.team.dataaccesslayer.Team.TeamRepository;
import com.athletics.team.dataaccesslayer.Team.TeamLevelEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Sql({"/data-h2.sql"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TeamControllerIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private TeamRepository teamRepository;

    private final String BASE_URI = "/api/v1/teams";

    private final String VALID_TEAM_ID    = "11111111-1111-1111-1111-111111111111";
    private final String NOT_FOUND_ID     = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    private final String INVALID_TEAM_ID  = "short-id";

    @Test
    public void whenTeamsExist_thenReturnAllTeams() {
        long count = teamRepository.count();

        webClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(TeamResponseModel.class)
                .value(list -> {
                    assertNotNull(list);
                    assertEquals(count, list.size());
                });
    }

    @Test
    public void whenCreateTeamNameAtMaxLength_thenReturnsCreated() {
        String maxName = "ABCDEFGHIJKLMNOPQRSTUVWX1234";
        TeamRequestModel newTeam = new TeamRequestModel(
                "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                maxName,
                "Coach Edge",
                TeamLevelEnum.COLLEGE
        );

        webClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(newTeam)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(TeamResponseModel.class)
                .value(team -> assertEquals(maxName, team.getTeamName()));
    }
    @Test
    public void whenCreateTeamNameTooLong_thenReturnUnprocessableEntity() {
        String longName = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDE";
        TeamRequestModel newTeam = new TeamRequestModel(
                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                longName,
                "Coach Long",
                TeamLevelEnum.COLLEGE
        );

        webClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(newTeam)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Team name cannot exceed 30 characters");
    }

    @Test
    public void whenGetByIdWithValidId_thenReturnTeam() {
        webClient.get()
                .uri(BASE_URI + "/" + VALID_TEAM_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(TeamResponseModel.class)
                .value(team -> {
                    assertEquals(VALID_TEAM_ID, team.getTeamId());
                    assertNotNull(team.getTeamName());
                    assertNotNull(team.getCoachName());
                    assertNotNull(team.getTeamLevel());
                });
    }

    @Test
    public  void whenGetByIdWithInvalidId_thenReturnUnprocessableEntity() {
        webClient.get()
                .uri(BASE_URI + "/" + INVALID_TEAM_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid teamId provided: " + INVALID_TEAM_ID);
    }

    @Test
    public void whenGetByIdNotFound_thenReturnNotFound() {
        webClient.get()
                .uri(BASE_URI + "/" + NOT_FOUND_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("No team found with ID: " + NOT_FOUND_ID);
    }

    @Test
    public void whenCreateTeamValid_thenReturnsCreated() {
        TeamRequestModel newTeam = new TeamRequestModel(
                "fedcba98-7654-3210-fedc-ba9876543210",
                "Regina Rams",
                "Pat Hughes",
                TeamLevelEnum.PROFESSIONAL
        );

        webClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(newTeam)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(TeamResponseModel.class)
                .value(team -> {
                    assertEquals(newTeam.getTeamId(), team.getTeamId());
                    assertEquals(newTeam.getTeamName(), team.getTeamName());
                    assertEquals(newTeam.getCoachName(), team.getCoachName());
                    assertEquals(newTeam.getTeamLevel(), team.getTeamLevel());
                });
    }

    @Test
    public  void whenCreateTeamDuplicate_thenReturnUnprocessableEntity() {
        TeamRequestModel dup = new TeamRequestModel(
                VALID_TEAM_ID,
                "Duplicate Team",
                "Coach X",
                TeamLevelEnum.COLLEGE
        );

        webClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(dup)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Team with ID already exists: " + VALID_TEAM_ID);
    }

    @Test
    public void whenUpdateTeamValid_thenReturnsCreated() {
        TeamRequestModel update = new TeamRequestModel(
                VALID_TEAM_ID,
                "Montreal Eagles Updated",
                "John Smith Jr.",
                TeamLevelEnum.NATIONAL
        );

        webClient.put()
                .uri(BASE_URI + "/" + VALID_TEAM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(TeamResponseModel.class)
                .value(team -> {
                    assertEquals(update.getTeamName(), team.getTeamName());
                    assertEquals(update.getCoachName(), team.getCoachName());
                    assertEquals(update.getTeamLevel(), team.getTeamLevel());
                });
    }

    @Test
    public  void whenUpdateWithInvalidId_thenReturnUnprocessableEntity() {
        TeamRequestModel update = new TeamRequestModel(
                INVALID_TEAM_ID,
                "X",
                "Y",
                TeamLevelEnum.HIGH_SCHOOL
        );

        webClient.put()
                .uri(BASE_URI + "/" + INVALID_TEAM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid teamId provided: " + INVALID_TEAM_ID);
    }

    @Test
    public  void whenUpdateNotFound_thenReturnNotFound() {
        TeamRequestModel update = new TeamRequestModel(
                NOT_FOUND_ID,
                "Nonexistent",
                "No Coach",
                TeamLevelEnum.COLLEGE
        );

        webClient.put()
                .uri(BASE_URI + "/" + NOT_FOUND_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("No team found with ID: " + NOT_FOUND_ID);
    }

    @Test
    public  void whenDeleteTeamValid_thenReturnNoContent() {
        webClient.delete()
                .uri(BASE_URI + "/" + VALID_TEAM_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        webClient.get()
                .uri(BASE_URI + "/" + VALID_TEAM_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public  void whenDeleteWithInvalidId_thenReturnUnprocessableEntity() {
        webClient.delete()
                .uri(BASE_URI + "/" + INVALID_TEAM_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid teamId provided: " + INVALID_TEAM_ID);
    }

    @Test
    public  void whenDeleteNotFound_thenReturnNotFound() {
        webClient.delete()
                .uri(BASE_URI + "/" + NOT_FOUND_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("No team found with ID: " + NOT_FOUND_ID);
    }
}
