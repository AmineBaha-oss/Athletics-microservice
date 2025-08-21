package com.athletics.team.presentationlayer.Athlete;

import com.athletics.team.dataaccesslayer.Athlete.AthleteRepository;
import com.athletics.team.dataaccesslayer.Athlete.AthleteCategoryEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Sql({"/data-h2.sql"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AthleteControllerIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private AthleteRepository athleteRepository;

    private final String BASE = "/api/v1";

    private final String VALID_TEAM_ID            = "11111111-1111-1111-1111-111111111111";
    private final String VALID_ATHLETE_ID         = "ath11111-1111-1111-1111-111111111111";
    private final String ANOTHER_ATHLETE_ID       = "ath22222-2222-2222-2222-222222222222";
    private final String NOT_FOUND_ATHLETE_ID     = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
    private final String INVALID_TEAM_ID          = "short-team-id";
    private final String INVALID_ATHLETE_ID       = "bad-ath-id";

    @Test
    public void whenGetAllAthletesForTeam_thenReturnList() {
        int expectedCount = athleteRepository.findByTeamId(VALID_TEAM_ID).size();

        webClient.get()
                .uri(BASE + "/" + VALID_TEAM_ID + "/athletes")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(AthleteResponseModel.class)
                .value(list -> assertEquals(expectedCount, list.size()));
    }

    @Test
    public void whenGetAllWithInvalidTeamId_thenReturnUnprocessableEntity() {
        webClient.get()
                .uri(BASE + "/" + INVALID_TEAM_ID + "/athletes")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid teamId provided: " + INVALID_TEAM_ID);
    }

    @Test
    public void whenGetAthleteByIdValid_thenReturnAthlete() {
        webClient.get()
                .uri(BASE + "/" + VALID_TEAM_ID + "/athletes/" + VALID_ATHLETE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AthleteResponseModel.class)
                .value(a -> {
                    assertEquals(VALID_ATHLETE_ID, a.getAthleteId());
                    assertEquals("Michael",      a.getFirstName());
                    assertEquals("Jordan",       a.getLastName());
                    assertEquals(LocalDate.of(1995, 1, 15), a.getDateOfBirth());
                    assertEquals(AthleteCategoryEnum.SENIOR, a.getAthleteCategory());
                });
    }

    @Test
    public  void whenGetByIdInvalidTeam_then422() {
        webClient.get()
                .uri(BASE + "/" + INVALID_TEAM_ID + "/athletes/" + VALID_ATHLETE_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid teamId provided: " + INVALID_TEAM_ID);
    }

    @Test
    public  void whenGetByIdInvalidAthlete_then422() {
        webClient.get()
                .uri(BASE + "/" + VALID_TEAM_ID + "/athletes/" + INVALID_ATHLETE_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid athleteId provided: " + INVALID_ATHLETE_ID);
    }

    @Test
    public  void whenGetByIdNotFound_then404() {
        webClient.get()
                .uri(BASE + "/" + VALID_TEAM_ID + "/athletes/" + NOT_FOUND_ATHLETE_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Athlete not found with teamId: "
                        + VALID_TEAM_ID
                        + " and athleteId: "
                        + NOT_FOUND_ATHLETE_ID);
    }

    @Test
    public void whenCreateAthleteValid_thenReturnsCreated() {
        AthleteRequestModel req = new AthleteRequestModel(
                "Alice",
                "Walker",
                LocalDate.of(2002, 2, 2),
                AthleteCategoryEnum.SENIOR,
                null
        );

        webClient.post()
                .uri(BASE + "/" + VALID_TEAM_ID + "/athletes")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AthleteResponseModel.class)
                .value(a -> {
                    assertNotNull(a.getAthleteId());
                    assertEquals(req.getFirstName(), a.getFirstName());
                    assertEquals(req.getLastName(),  a.getLastName());
                    assertEquals(req.getDateOfBirth(), a.getDateOfBirth());
                    assertEquals(req.getAthleteCategory(), a.getAthleteCategory());
                });
    }

    @Test
    public void whenUpdateAthleteValid_thenReturnsCreated() {
        AthleteRequestModel update = new AthleteRequestModel(
                "Michael",
                "Jordan Jr.",
                LocalDate.of(1995, 1, 15),
                AthleteCategoryEnum.SENIOR,
                null
        );

        webClient.put()
                .uri(BASE + "/" + VALID_TEAM_ID + "/athletes/" + VALID_ATHLETE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AthleteResponseModel.class)
                .value(a -> {
                    assertEquals(VALID_ATHLETE_ID, a.getAthleteId());
                    assertEquals(update.getLastName(),  a.getLastName());
                });
    }

    @Test
    public  void whenUpdateInvalidTeam_then422() {
        AthleteRequestModel any = new AthleteRequestModel("X","Y",LocalDate.now(), AthleteCategoryEnum.JUNIOR, null);

        webClient.put()
                .uri(BASE + "/" + INVALID_TEAM_ID + "/athletes/" + VALID_ATHLETE_ID)
                .bodyValue(any)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid teamId provided: " + INVALID_TEAM_ID);
    }

    @Test
    public  void whenUpdateInvalidAthlete_then422() {
        AthleteRequestModel any = new AthleteRequestModel("X","Y",LocalDate.now(), AthleteCategoryEnum.JUNIOR, null);

        webClient.put()
                .uri(BASE + "/" + VALID_TEAM_ID + "/athletes/" + INVALID_ATHLETE_ID)
                .bodyValue(any)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid athleteId provided: " + INVALID_ATHLETE_ID);
    }

    @Test
    public void whenUpdateNotFound_then404() {
        AthleteRequestModel any = new AthleteRequestModel("X","Y",LocalDate.now(), AthleteCategoryEnum.JUNIOR, null);

        webClient.put()
                .uri(BASE + "/" + VALID_TEAM_ID + "/athletes/" + NOT_FOUND_ATHLETE_ID)
                .bodyValue(any)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Athlete not found with teamId: "
                        + VALID_TEAM_ID
                        + " and athleteId: "
                        + NOT_FOUND_ATHLETE_ID);
    }

    @Test
    public void whenDeleteAthleteValid_thenReturnNoContent() {
        webClient.delete()
                .uri(BASE + "/" + VALID_TEAM_ID + "/athletes/" + ANOTHER_ATHLETE_ID)
                .exchange()
                .expectStatus().isNoContent();

        // then GET by id yields 404
        webClient.get()
                .uri(BASE + "/" + VALID_TEAM_ID + "/athletes/" + ANOTHER_ATHLETE_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void whenDeleteInvalidTeam_then422() {
        webClient.delete()
                .uri(BASE + "/" + INVALID_TEAM_ID + "/athletes/" + VALID_ATHLETE_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid teamId provided: " + INVALID_TEAM_ID);
    }

    @Test
    public void whenDeleteInvalidAthlete_then422() {
        webClient.delete()
                .uri(BASE + "/" + VALID_TEAM_ID + "/athletes/" + INVALID_ATHLETE_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid athleteId provided: " + INVALID_ATHLETE_ID);
    }

    @Test
    public  void whenDeleteNotFound_then404() {
        webClient.delete()
                .uri(BASE + "/" + VALID_TEAM_ID + "/athletes/" + NOT_FOUND_ATHLETE_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Athlete not found with teamId: "
                        + VALID_TEAM_ID
                        + " and athleteId: "
                        + NOT_FOUND_ATHLETE_ID);
    }
}
