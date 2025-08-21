package com.athletics.competition.presentationlayer;

import com.athletics.competition.businesslayer.CompetitionService;
import com.athletics.competition.presentationlayer.CompetitionRequestModel;
import com.athletics.competition.presentationlayer.CompetitionResponseModel;
import com.athletics.competition.utils.exceptions.InvalidInputException;
import com.athletics.competition.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private CompetitionController controller;

    @MockitoBean
    private CompetitionService service;

    private static final String VALID_TEAM_ID        = "11111111-1111-1111-1111-111111111111";
    private static final String INVALID_TEAM_ID      = "bad-uuid";
    private static final String VALID_COMPETITION_ID = "22222222-2222-2222-2222-222222222222";
    private static final String NOT_FOUND_COMPETITION_ID = "33333333-3333-3333-3333-333333333333";

    @Test
    public void whenNoCompetitionsExist_thenReturnEmptyList() {
        when(service.getAllCompetitions(VALID_TEAM_ID)).thenReturn(Collections.emptyList());

        ResponseEntity<List<CompetitionResponseModel>> response =
                controller.getAllCompetitions(VALID_TEAM_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(service).getAllCompetitions(VALID_TEAM_ID);
    }

    @Test
    public void whenGetAllCompetitionsWithInvalidTeamId_thenThrowInvalidInputException() {
        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> controller.getAllCompetitions(INVALID_TEAM_ID)
        );
        assertEquals("Invalid teamId provided: " + INVALID_TEAM_ID, ex.getMessage());
        verify(service, never()).getAllCompetitions(any());
    }

    @Test
    public void whenGetCompetitionByIdValid_thenReturnCompetition() {
        CompetitionResponseModel dto = new CompetitionResponseModel();
        dto.setCompetitionId(VALID_COMPETITION_ID);
        when(service.getCompetitionById(VALID_TEAM_ID, VALID_COMPETITION_ID)).thenReturn(dto);

        ResponseEntity<CompetitionResponseModel> response =
                controller.getCompetitionById(VALID_TEAM_ID, VALID_COMPETITION_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(VALID_COMPETITION_ID, response.getBody().getCompetitionId());
        verify(service).getCompetitionById(VALID_TEAM_ID, VALID_COMPETITION_ID);
    }

    @Test
    public void whenGetCompetitionByIdWithInvalidCompetitionId_thenThrowInvalidInputException() {
        doThrow(new InvalidInputException("Invalid ID provided"))
                .when(service).getCompetitionById(VALID_TEAM_ID, INVALID_TEAM_ID);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> controller.getCompetitionById(VALID_TEAM_ID, INVALID_TEAM_ID)
        );
        assertEquals("Invalid ID provided", ex.getMessage());
    }

    @Test
    public void whenGetCompetitionByIdNotFound_thenThrowNotFoundException() {
        doThrow(new NotFoundException("not found"))
                .when(service).getCompetitionById(VALID_TEAM_ID, NOT_FOUND_COMPETITION_ID);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> controller.getCompetitionById(VALID_TEAM_ID, NOT_FOUND_COMPETITION_ID)
        );
        assertEquals("not found", ex.getMessage());
    }

    @Test
    public void whenCreateCompetitionWithValidData_thenReturnCreated() {
        CompetitionRequestModel req = new CompetitionRequestModel();
        CompetitionResponseModel created = new CompetitionResponseModel();
        when(service.createCompetition(VALID_TEAM_ID, req)).thenReturn(created);

        ResponseEntity<CompetitionResponseModel> response =
                controller.createCompetition(VALID_TEAM_ID, req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(created, response.getBody());
        verify(service).createCompetition(VALID_TEAM_ID, req);
    }

    @Test
    public void whenCreateCompetitionWithInvalidTeamId_thenThrowInvalidInputException() {
        CompetitionRequestModel req = new CompetitionRequestModel();
        doThrow(new InvalidInputException("Invalid teamId provided: " + INVALID_TEAM_ID))
                .when(service).createCompetition(INVALID_TEAM_ID, req);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> controller.createCompetition(INVALID_TEAM_ID, req)
        );
        assertEquals("Invalid teamId provided: " + INVALID_TEAM_ID, ex.getMessage());
    }

    @Test
    public void whenUpdateCompetitionWithValidData_thenReturnOk() {
        CompetitionRequestModel req = new CompetitionRequestModel();
        CompetitionResponseModel updated = new CompetitionResponseModel();
        when(service.updateCompetition(VALID_TEAM_ID, VALID_COMPETITION_ID, req)).thenReturn(updated);

        ResponseEntity<CompetitionResponseModel> response =
                controller.updateCompetition(VALID_TEAM_ID, VALID_COMPETITION_ID, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(updated, response.getBody());
        verify(service).updateCompetition(VALID_TEAM_ID, VALID_COMPETITION_ID, req);
    }

    @Test
    public void whenUpdateCompetitionWithInvalidCompetitionId_thenThrowInvalidInputException() {
        CompetitionRequestModel req = new CompetitionRequestModel();
        doThrow(new InvalidInputException("Invalid ID provided"))
                .when(service).updateCompetition(VALID_TEAM_ID, INVALID_TEAM_ID, req);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> controller.updateCompetition(VALID_TEAM_ID, INVALID_TEAM_ID, req)
        );
        assertEquals("Invalid ID provided", ex.getMessage());
    }

    @Test
    public void whenUpdateCompetitionNotFound_thenThrowNotFoundException() {
        CompetitionRequestModel req = new CompetitionRequestModel();
        doThrow(new NotFoundException("not found"))
                .when(service).updateCompetition(VALID_TEAM_ID, NOT_FOUND_COMPETITION_ID, req);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> controller.updateCompetition(VALID_TEAM_ID, NOT_FOUND_COMPETITION_ID, req)
        );
        assertEquals("not found", ex.getMessage());
    }

    @Test
    public void whenDeleteCompetitionWithValidData_thenReturnNoContent() {
        doNothing().when(service).deleteCompetition(VALID_TEAM_ID, VALID_COMPETITION_ID);

        ResponseEntity<Void> response =
                controller.deleteCompetition(VALID_TEAM_ID, VALID_COMPETITION_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service).deleteCompetition(VALID_TEAM_ID, VALID_COMPETITION_ID);
    }

    @Test
    public void whenDeleteCompetitionWithInvalidCompetitionId_thenThrowInvalidInputException() {
        doThrow(new InvalidInputException("Invalid ID provided"))
                .when(service).deleteCompetition(VALID_TEAM_ID, INVALID_TEAM_ID);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> controller.deleteCompetition(VALID_TEAM_ID, INVALID_TEAM_ID)
        );
        assertEquals("Invalid ID provided", ex.getMessage());
    }

    @Test
    public void whenDeleteCompetitionNotFound_thenThrowNotFoundException() {
        doThrow(new NotFoundException("not found"))
                .when(service).deleteCompetition(VALID_TEAM_ID, NOT_FOUND_COMPETITION_ID);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> controller.deleteCompetition(VALID_TEAM_ID, NOT_FOUND_COMPETITION_ID)
        );
        assertEquals("not found", ex.getMessage());
    }
}
