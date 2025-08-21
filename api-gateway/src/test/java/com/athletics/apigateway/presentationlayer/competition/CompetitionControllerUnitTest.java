package com.athletics.apigateway.presentationlayer.competition;

import com.athletics.apigateway.businesslayer.competition.CompetitionService;
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
class CompetitionControllerUnitTest {

    @Autowired
    private CompetitionController competitionController;

    @MockitoBean
    private CompetitionService competitionService;

    private final String VALID_TEAM_ID            = "11111111-1111-1111-1111-111111111111";
    private final String INVALID_TEAM_ID          = "bad-team-id";
    private final String VALID_COMPETITION_ID     = "22222222-2222-2222-2222-222222222222";
    private final String INVALID_COMPETITION_ID   = "bad-comp-id";
    private final String NOT_FOUND_COMPETITION_ID = "44444444-4444-4444-4444-444444444444";

    @Test
    void whenNoCompetitionsExist_thenReturnEmptyList() {
        when(competitionService.getAllCompetitions(VALID_TEAM_ID))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<CompetitionResponseModel>> resp =
                competitionController.getAllCompetitions(VALID_TEAM_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isEmpty());
        verify(competitionService).getAllCompetitions(VALID_TEAM_ID);
    }

    @Test
    void whenGetAllWithInvalidTeam_thenThrowInvalidInput() {
        doThrow(new InvalidInputException("Invalid teamId: " + INVALID_TEAM_ID))
                .when(competitionService).getAllCompetitions(INVALID_TEAM_ID);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> competitionController.getAllCompetitions(INVALID_TEAM_ID)
        );
        assertEquals("Invalid teamId: " + INVALID_TEAM_ID, ex.getMessage());
        verify(competitionService).getAllCompetitions(INVALID_TEAM_ID);
    }

    @Test
    void whenGetByIdValid_thenReturnCompetition() {
        CompetitionResponseModel mockComp = CompetitionResponseModel.builder()
                .competitionId(VALID_COMPETITION_ID)
                .competitionName("Spring Invitational")
                .competitionDate(LocalDate.of(2025, 6, 1))
                .competitionStatus("SCHEDULED")
                .competitionResult("DRAW")
                .teamId(VALID_TEAM_ID)
                .build();

        when(competitionService.getCompetitionById(VALID_TEAM_ID, VALID_COMPETITION_ID))
                .thenReturn(mockComp);

        ResponseEntity<CompetitionResponseModel> resp =
                competitionController.getCompetitionById(VALID_TEAM_ID, VALID_COMPETITION_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Spring Invitational", resp.getBody().getCompetitionName());
        verify(competitionService).getCompetitionById(VALID_TEAM_ID, VALID_COMPETITION_ID);
    }

    @Test
    void whenGetByIdInvalidComp_thenThrowInvalidInput() {
        doThrow(new InvalidInputException("Invalid competitionId: " + INVALID_COMPETITION_ID))
                .when(competitionService).getCompetitionById(VALID_TEAM_ID, INVALID_COMPETITION_ID);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> competitionController.getCompetitionById(VALID_TEAM_ID, INVALID_COMPETITION_ID)
        );
        assertEquals("Invalid competitionId: " + INVALID_COMPETITION_ID, ex.getMessage());
        verify(competitionService).getCompetitionById(VALID_TEAM_ID, INVALID_COMPETITION_ID);
    }

    @Test
    void whenGetByIdNotFound_thenThrowNotFound() {
        doThrow(new NotFoundException(
                "Competition not found with teamId: " +
                        VALID_TEAM_ID +
                        " and competitionId: " +
                        NOT_FOUND_COMPETITION_ID
        )).when(competitionService)
                .getCompetitionById(VALID_TEAM_ID, NOT_FOUND_COMPETITION_ID);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> competitionController.getCompetitionById(VALID_TEAM_ID, NOT_FOUND_COMPETITION_ID)
        );
        assertTrue(ex.getMessage().contains("Competition not found with teamId"));
        verify(competitionService).getCompetitionById(VALID_TEAM_ID, NOT_FOUND_COMPETITION_ID);
    }

    @Test
    void whenCreateValid_thenReturnCreated() {
        CompetitionRequestModel req = new CompetitionRequestModel(
                "Winter Classic",
                LocalDate.of(2025, 12, 5),
                "SCHEDULED",
                "DRAW",
                null,
                null
        );
        CompetitionResponseModel created = CompetitionResponseModel.builder()
                .competitionId(VALID_COMPETITION_ID)
                .competitionName("Winter Classic")
                .competitionDate(LocalDate.of(2025, 12, 5))
                .competitionStatus("SCHEDULED")
                .competitionResult("DRAW")
                .teamId(VALID_TEAM_ID)
                .build();

        when(competitionService.createCompetition(VALID_TEAM_ID, req))
                .thenReturn(created);

        ResponseEntity<CompetitionResponseModel> resp =
                competitionController.createCompetition(VALID_TEAM_ID, req);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertEquals(VALID_COMPETITION_ID, resp.getBody().getCompetitionId());
        verify(competitionService).createCompetition(VALID_TEAM_ID, req);
    }

    @Test
    void whenCreateWithInvalidTeam_thenThrowInvalidInput() {
        CompetitionRequestModel req = new CompetitionRequestModel(
                "Test Competition",
                LocalDate.now(),
                "SCHEDULED",
                "LOSS",
                null,
                null
        );
        doThrow(new InvalidInputException("Invalid teamId: " + INVALID_TEAM_ID))
                .when(competitionService).createCompetition(INVALID_TEAM_ID, req);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> competitionController.createCompetition(INVALID_TEAM_ID, req)
        );
        assertEquals("Invalid teamId: " + INVALID_TEAM_ID, ex.getMessage());
        verify(competitionService).createCompetition(INVALID_TEAM_ID, req);
    }

    @Test
    void whenUpdateValid_thenReturnOk() {
        CompetitionRequestModel req = new CompetitionRequestModel(
                "Championship Final",
                LocalDate.of(2025, 11, 20),
                "COMPLETED",
                "WIN",
                null,
                null
        );
        CompetitionResponseModel updated = CompetitionResponseModel.builder()
                .competitionId(VALID_COMPETITION_ID)
                .competitionName("Championship Final")
                .competitionDate(LocalDate.of(2025, 11, 20))
                .competitionStatus("COMPLETED")
                .competitionResult("WIN")
                .teamId(VALID_TEAM_ID)
                .build();

        when(competitionService.updateCompetition(VALID_TEAM_ID, VALID_COMPETITION_ID, req))
                .thenReturn(updated);

        ResponseEntity<CompetitionResponseModel> resp =
                competitionController.updateCompetition(VALID_TEAM_ID, VALID_COMPETITION_ID, req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Championship Final", resp.getBody().getCompetitionName());
        verify(competitionService).updateCompetition(VALID_TEAM_ID, VALID_COMPETITION_ID, req);
    }

    @Test
    void whenUpdateInvalidComp_thenThrowInvalidInput() {
        CompetitionRequestModel req = new CompetitionRequestModel(
                "X", LocalDate.now(), "SCHEDULED", "LOSS", null, null
        );
        doThrow(new InvalidInputException("Invalid competitionId: " + INVALID_COMPETITION_ID))
                .when(competitionService).updateCompetition(VALID_TEAM_ID, INVALID_COMPETITION_ID, req);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> competitionController.updateCompetition(VALID_TEAM_ID, INVALID_COMPETITION_ID, req)
        );
        assertEquals("Invalid competitionId: " + INVALID_COMPETITION_ID, ex.getMessage());
        verify(competitionService).updateCompetition(VALID_TEAM_ID, INVALID_COMPETITION_ID, req);
    }

    @Test
    void whenUpdateNotFound_thenThrowNotFound() {
        CompetitionRequestModel req = new CompetitionRequestModel(
                "Y", LocalDate.now(), "SCHEDULED", "LOSS", null, null
        );
        doThrow(new NotFoundException(
                "Competition not found with teamId: " +
                        VALID_TEAM_ID + " and competitionId: " +
                        NOT_FOUND_COMPETITION_ID
        )).when(competitionService)
                .updateCompetition(VALID_TEAM_ID, NOT_FOUND_COMPETITION_ID, req);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> competitionController.updateCompetition(VALID_TEAM_ID, NOT_FOUND_COMPETITION_ID, req)
        );
        assertTrue(ex.getMessage().contains("Competition not found with teamId"));
        verify(competitionService).updateCompetition(VALID_TEAM_ID, NOT_FOUND_COMPETITION_ID, req);
    }

    @Test
    void whenDeleteValid_thenReturnNoContent() {
        doNothing().when(competitionService)
                .deleteCompetition(VALID_TEAM_ID, VALID_COMPETITION_ID);

        ResponseEntity<Void> resp =
                competitionController.deleteCompetition(VALID_TEAM_ID, VALID_COMPETITION_ID);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(competitionService).deleteCompetition(VALID_TEAM_ID, VALID_COMPETITION_ID);
    }

    @Test
    void whenDeleteInvalidComp_thenThrowInvalidInput() {
        doThrow(new InvalidInputException("Invalid competitionId: " + INVALID_COMPETITION_ID))
                .when(competitionService).deleteCompetition(VALID_TEAM_ID, INVALID_COMPETITION_ID);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> competitionController.deleteCompetition(VALID_TEAM_ID, INVALID_COMPETITION_ID)
        );
        assertEquals("Invalid competitionId: " + INVALID_COMPETITION_ID, ex.getMessage());
        verify(competitionService).deleteCompetition(VALID_TEAM_ID, INVALID_COMPETITION_ID);
    }

    @Test
    void whenDeleteNotFound_thenThrowNotFound() {
        doThrow(new NotFoundException(
                "Competition not found with teamId: " +
                        VALID_TEAM_ID + " and competitionId: " +
                        NOT_FOUND_COMPETITION_ID
        )).when(competitionService)
                .deleteCompetition(VALID_TEAM_ID, NOT_FOUND_COMPETITION_ID);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> competitionController.deleteCompetition(VALID_TEAM_ID, NOT_FOUND_COMPETITION_ID)
        );
        assertTrue(ex.getMessage().contains("Competition not found with teamId"));
        verify(competitionService).deleteCompetition(VALID_TEAM_ID, NOT_FOUND_COMPETITION_ID);
    }
}
