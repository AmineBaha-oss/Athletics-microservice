package com.athletics.apigateway.presentationlayer.team;

import com.athletics.apigateway.businesslayer.team.TeamService;
import com.athletics.apigateway.utils.exceptions.InvalidInputException;
import com.athletics.apigateway.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class TeamControllerUnitTest {

    @Autowired
    private TeamController teamController;

    @MockitoBean
    private TeamService teamService;

    private final String VALID_TEAM_ID       = "11111111-1111-1111-1111-111111111111";
    private final String NOT_FOUND_TEAM_ID   = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    private final String INVALID_TEAM_ID     = "bad-team-id";

    @Test
    public void whenNoTeamsExist_thenReturnEmptyList() {
        when(teamService.getAllTeams()).thenReturn(Collections.emptyList());

        ResponseEntity<List<TeamResponseModel>> response = teamController.getAllTeams();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(teamService, times(1)).getAllTeams();
    }

    @Test
    public void whenGetTeamByIdValid_thenReturnTeam() {
        TeamResponseModel mockTeam = new TeamResponseModel();
        mockTeam.setTeamId(VALID_TEAM_ID);
        mockTeam.setTeamName("Dream Team");

        when(teamService.getTeamById(VALID_TEAM_ID)).thenReturn(mockTeam);

        ResponseEntity<TeamResponseModel> response = teamController.getTeamById(VALID_TEAM_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(VALID_TEAM_ID, response.getBody().getTeamId());
        assertEquals("Dream Team", response.getBody().getTeamName());
        verify(teamService, times(1)).getTeamById(VALID_TEAM_ID);
    }

    @Test
    public void whenTeamIdInvalid_thenThrowInvalidInputException() {
        doThrow(new InvalidInputException("Invalid teamId provided: " + INVALID_TEAM_ID))
                .when(teamService).getTeamById(INVALID_TEAM_ID);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> teamController.getTeamById(INVALID_TEAM_ID)
        );
        assertEquals("Invalid teamId provided: " + INVALID_TEAM_ID, ex.getMessage());
        verify(teamService, times(1)).getTeamById(INVALID_TEAM_ID);
    }

    @Test
    public void whenGetTeamNotFound_thenThrowNotFoundException() {
        doThrow(new NotFoundException("No team found with ID: " + NOT_FOUND_TEAM_ID))
                .when(teamService).getTeamById(NOT_FOUND_TEAM_ID);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> teamController.getTeamById(NOT_FOUND_TEAM_ID)
        );
        assertEquals("No team found with ID: " + NOT_FOUND_TEAM_ID, ex.getMessage());
        verify(teamService, times(1)).getTeamById(NOT_FOUND_TEAM_ID);
    }

    @Test
    public void whenCreateTeamValid_thenReturnsCreated() {
        TeamRequestModel req = new TeamRequestModel();
        req.setTeamName("Champions");

        TeamResponseModel created = new TeamResponseModel();
        created.setTeamId("new-id");
        created.setTeamName("Champions");

        when(teamService.createTeam(req)).thenReturn(created);

        ResponseEntity<TeamResponseModel> response = teamController.createTeam(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("new-id", response.getBody().getTeamId());
        assertEquals("Champions", response.getBody().getTeamName());
        verify(teamService, times(1)).createTeam(req);
    }

    @Test
    public void whenCreateTeamInvalid_thenThrowInvalidInputException() {
        TeamRequestModel bad = new TeamRequestModel();
        bad.setTeamName("");

        doThrow(new InvalidInputException("Invalid teamName"))
                .when(teamService).createTeam(bad);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> teamController.createTeam(bad)
        );
        assertEquals("Invalid teamName", ex.getMessage());
        verify(teamService, times(1)).createTeam(bad);
    }

    @Test
    public void whenUpdateTeamValid_thenReturnsOk() {
        TeamRequestModel update = new TeamRequestModel();
        update.setTeamName("All-Stars");

        TeamResponseModel updated = new TeamResponseModel();
        updated.setTeamId(VALID_TEAM_ID);
        updated.setTeamName("All-Stars");

        when(teamService.updateTeam(VALID_TEAM_ID, update)).thenReturn(updated);

        ResponseEntity<TeamResponseModel> response = teamController.updateTeam(VALID_TEAM_ID, update);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(VALID_TEAM_ID, response.getBody().getTeamId());
        assertEquals("All-Stars", response.getBody().getTeamName());
        verify(teamService, times(1)).updateTeam(VALID_TEAM_ID, update);
    }

    @Test
    public void whenUpdateWithInvalidTeam_thenThrowInvalidInputException() {
        TeamRequestModel update = new TeamRequestModel();
        update.setTeamName("X");

        doThrow(new InvalidInputException("Invalid teamId provided: " + INVALID_TEAM_ID))
                .when(teamService).updateTeam(INVALID_TEAM_ID, update);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> teamController.updateTeam(INVALID_TEAM_ID, update)
        );
        assertEquals("Invalid teamId provided: " + INVALID_TEAM_ID, ex.getMessage());
        verify(teamService, times(1)).updateTeam(INVALID_TEAM_ID, update);
    }

    @Test
    public void whenUpdateNotFound_thenThrowNotFoundException() {
        TeamRequestModel update = new TeamRequestModel();
        update.setTeamName("Y");

        doThrow(new NotFoundException("No team found with ID: " + NOT_FOUND_TEAM_ID))
                .when(teamService).updateTeam(NOT_FOUND_TEAM_ID, update);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> teamController.updateTeam(NOT_FOUND_TEAM_ID, update)
        );
        assertEquals("No team found with ID: " + NOT_FOUND_TEAM_ID, ex.getMessage());
        verify(teamService, times(1)).updateTeam(NOT_FOUND_TEAM_ID, update);
    }


    @Test
    public void whenTeamDeleted_thenReturnNoContent() {
        when(teamService.deleteTeam(VALID_TEAM_ID)).thenReturn(null);

        ResponseEntity<Void> response = teamController.deleteTeam(VALID_TEAM_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(teamService, times(1)).deleteTeam(VALID_TEAM_ID);
    }

    @Test
    public void whenDeleteInvalidTeam_thenThrowInvalidInputException() {
        doThrow(new InvalidInputException("Invalid teamId provided: " + INVALID_TEAM_ID))
                .when(teamService).deleteTeam(INVALID_TEAM_ID);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> teamController.deleteTeam(INVALID_TEAM_ID)
        );
        assertEquals("Invalid teamId provided: " + INVALID_TEAM_ID, ex.getMessage());
        verify(teamService, times(1)).deleteTeam(INVALID_TEAM_ID);
    }

    @Test
    public void whenDeleteNotFound_thenThrowNotFoundException() {
        doThrow(new NotFoundException("No team found with ID: " + NOT_FOUND_TEAM_ID))
                .when(teamService).deleteTeam(NOT_FOUND_TEAM_ID);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> teamController.deleteTeam(NOT_FOUND_TEAM_ID)
        );
        assertEquals("No team found with ID: " + NOT_FOUND_TEAM_ID, ex.getMessage());
        verify(teamService, times(1)).deleteTeam(NOT_FOUND_TEAM_ID);
    }
}
