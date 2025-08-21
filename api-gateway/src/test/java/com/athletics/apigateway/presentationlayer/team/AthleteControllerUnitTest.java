package com.athletics.apigateway.presentationlayer.team;

import com.athletics.apigateway.businesslayer.team.TeamService;
import com.athletics.apigateway.domainclientlayer.team.AthleteCategoryEnum;
import com.athletics.apigateway.utils.exceptions.InvalidInputException;
import com.athletics.apigateway.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class AthleteControllerUnitTest {

    @Autowired
    private AthleteController athleteController;

    @MockitoBean
    private TeamService teamService;

    private final String VALID_TEAM_ID        = "11111111-1111-1111-1111-111111111111";
    private final String INVALID_TEAM_ID      = "bad-team-id";
    private final String VALID_ATHLETE_ID     = "ath11111-1111-1111-1111-111111111111";
    private final String INVALID_ATHLETE_ID   = "bad-ath-id";
    private final String NOT_FOUND_ATHLETE_ID = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

    @Test
    void whenNoAthletesExist_thenReturnEmptyList() {
        when(teamService.getAllAthletes(VALID_TEAM_ID))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<AthleteResponseModel>> resp =
                athleteController.getAllAthletes(VALID_TEAM_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isEmpty());
        verify(teamService).getAllAthletes(VALID_TEAM_ID);
    }

    @Test
    void whenGetAllWithInvalidTeam_thenThrowInvalidInput() {
        doThrow(new InvalidInputException("Invalid teamId provided: " + INVALID_TEAM_ID))
                .when(teamService).getAllAthletes(INVALID_TEAM_ID);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> athleteController.getAllAthletes(INVALID_TEAM_ID)
        );
        assertEquals("Invalid teamId provided: " + INVALID_TEAM_ID, ex.getMessage());
        verify(teamService).getAllAthletes(INVALID_TEAM_ID);
    }

    @Test
    void whenGetByIdValid_thenReturnAthlete() {
        AthleteResponseModel mockAth = new AthleteResponseModel(
                VALID_ATHLETE_ID,
                "Alice",
                "Smith",
                LocalDate.of(2001,1,1),
                AthleteCategoryEnum.JUNIOR
        );
        when(teamService.getAthleteById(VALID_TEAM_ID, VALID_ATHLETE_ID))
                .thenReturn(mockAth);

        ResponseEntity<AthleteResponseModel> resp =
                athleteController.getAthleteById(VALID_TEAM_ID, VALID_ATHLETE_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Alice", resp.getBody().getFirstName());
        verify(teamService).getAthleteById(VALID_TEAM_ID, VALID_ATHLETE_ID);
    }

    @Test
    void whenGetByIdInvalidAthlete_thenThrowInvalidInput() {
        doThrow(new InvalidInputException("Invalid athleteId provided: " + INVALID_ATHLETE_ID))
                .when(teamService).getAthleteById(VALID_TEAM_ID, INVALID_ATHLETE_ID);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> athleteController.getAthleteById(VALID_TEAM_ID, INVALID_ATHLETE_ID)
        );
        assertEquals("Invalid athleteId provided: " + INVALID_ATHLETE_ID, ex.getMessage());
        verify(teamService).getAthleteById(VALID_TEAM_ID, INVALID_ATHLETE_ID);
    }

    @Test
    void whenGetByIdNotFound_thenThrowNotFound() {
        doThrow(new NotFoundException(
                "Athlete not found with teamId: " +
                        VALID_TEAM_ID +
                        " and athleteId: " +
                        NOT_FOUND_ATHLETE_ID
        )).when(teamService).getAthleteById(VALID_TEAM_ID, NOT_FOUND_ATHLETE_ID);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> athleteController.getAthleteById(VALID_TEAM_ID, NOT_FOUND_ATHLETE_ID)
        );
        assertTrue(ex.getMessage().contains("Athlete not found with teamId"));
        verify(teamService).getAthleteById(VALID_TEAM_ID, NOT_FOUND_ATHLETE_ID);
    }

    @Test
    void whenCreateValid_thenReturnCreated() {
        AthleteRequestModel req = new AthleteRequestModel(
                "Bob","Jones",
                LocalDate.of(2000,5,5),
                AthleteCategoryEnum.SENIOR
        );
        AthleteResponseModel created = new AthleteResponseModel(
                "new-athlete-id","Bob","Jones",
                LocalDate.of(2000,5,5),
                AthleteCategoryEnum.SENIOR
        );
        when(teamService.createAthlete(VALID_TEAM_ID, req)).thenReturn(created);

        ResponseEntity<AthleteResponseModel> resp =
                athleteController.createAthlete(VALID_TEAM_ID, req);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertEquals("new-athlete-id", resp.getBody().getAthleteId());
        verify(teamService).createAthlete(VALID_TEAM_ID, req);
    }

    @Test
    void whenCreateWithInvalidTeam_thenThrowInvalidInput() {
        AthleteRequestModel req = new AthleteRequestModel(
                "Bob","Jones",
                LocalDate.of(2000,5,5),
                AthleteCategoryEnum.SENIOR
        );
        doThrow(new InvalidInputException("Invalid teamId provided: " + INVALID_TEAM_ID))
                .when(teamService).createAthlete(INVALID_TEAM_ID, req);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> athleteController.createAthlete(INVALID_TEAM_ID, req)
        );
        assertEquals("Invalid teamId provided: " + INVALID_TEAM_ID, ex.getMessage());
        verify(teamService).createAthlete(INVALID_TEAM_ID, req);
    }

    @Test
    void whenDeleteValid_thenReturnNoContent() {
        doNothing().when(teamService)
                .deleteAthlete(VALID_TEAM_ID, VALID_ATHLETE_ID);

        ResponseEntity<Void> resp =
                athleteController.deleteAthlete(VALID_TEAM_ID, VALID_ATHLETE_ID);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(teamService).deleteAthlete(VALID_TEAM_ID, VALID_ATHLETE_ID);
    }

    @Test
    void whenDeleteInvalidAthlete_thenThrowInvalidInput() {
        doThrow(new InvalidInputException("Invalid athleteId provided: " + INVALID_ATHLETE_ID))
                .when(teamService).deleteAthlete(VALID_TEAM_ID, INVALID_ATHLETE_ID);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> athleteController.deleteAthlete(VALID_TEAM_ID, INVALID_ATHLETE_ID)
        );
        assertEquals("Invalid athleteId provided: " + INVALID_ATHLETE_ID, ex.getMessage());
        verify(teamService).deleteAthlete(VALID_TEAM_ID, INVALID_ATHLETE_ID);
    }

    @Test
    void whenUpdateValid_thenReturnOk() {
        AthleteRequestModel update = new AthleteRequestModel(
                "John","Doe Jr.",
                LocalDate.of(2000,1,1),
                AthleteCategoryEnum.SENIOR
        );
        AthleteResponseModel updated = new AthleteResponseModel(
                VALID_ATHLETE_ID, "John","Doe Jr.",
                LocalDate.of(2000,1,1),
                AthleteCategoryEnum.SENIOR
        );
        when(teamService.updateAthlete(VALID_TEAM_ID, VALID_ATHLETE_ID, update))
                .thenReturn(updated);

        ResponseEntity<AthleteResponseModel> resp =
                athleteController.updateAthlete(VALID_TEAM_ID, VALID_ATHLETE_ID, update);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Doe Jr.", resp.getBody().getLastName());
        verify(teamService).updateAthlete(VALID_TEAM_ID, VALID_ATHLETE_ID, update);
    }

    @Test
    void whenUpdateInvalidAthlete_thenThrowInvalidInput() {
        AthleteRequestModel any = new AthleteRequestModel(
                "X","Y", LocalDate.now(), AthleteCategoryEnum.JUNIOR
        );
        doThrow(new InvalidInputException("Invalid athleteId provided: " + INVALID_ATHLETE_ID))
                .when(teamService).updateAthlete(VALID_TEAM_ID, INVALID_ATHLETE_ID, any);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> athleteController.updateAthlete(VALID_TEAM_ID, INVALID_ATHLETE_ID, any)
        );
        assertEquals("Invalid athleteId provided: " + INVALID_ATHLETE_ID, ex.getMessage());
        verify(teamService).updateAthlete(VALID_TEAM_ID, INVALID_ATHLETE_ID, any);
    }

    @Test
    void whenUpdateNotFound_thenThrowNotFound() {
        AthleteRequestModel any = new AthleteRequestModel(
                "X","Y", LocalDate.now(), AthleteCategoryEnum.JUNIOR
        );
        doThrow(new NotFoundException(
                "Athlete not found with teamId: " +
                        VALID_TEAM_ID +
                        " and athleteId: " +
                        NOT_FOUND_ATHLETE_ID
        )).when(teamService).updateAthlete(VALID_TEAM_ID, NOT_FOUND_ATHLETE_ID, any);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> athleteController.updateAthlete(VALID_TEAM_ID, NOT_FOUND_ATHLETE_ID, any)
        );
        assertTrue(ex.getMessage().contains("Athlete not found with teamId"));
        verify(teamService).updateAthlete(VALID_TEAM_ID, NOT_FOUND_ATHLETE_ID, any);
    }
}
