package com.athletics.team.presentationlayer.Team;

import com.athletics.team.businesslayer.Team.TeamService;
import com.athletics.team.presentationlayer.Team.TeamRequestModel;
import com.athletics.team.presentationlayer.Team.TeamResponseModel;
import com.athletics.team.utils.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/teams")
public class TeamController {

    private final TeamService teamService;
    private static final int UUID_LENGTH = 36;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public ResponseEntity<List<TeamResponseModel>> getAllTeams() {
        return ResponseEntity.ok().body(teamService.getAllTeams());
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponseModel> getTeamById(@PathVariable String teamId) {
        if (teamId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid teamId provided: " + teamId);
        }
        return ResponseEntity.ok().body(teamService.getTeamById(teamId));
    }

    @PostMapping
    public ResponseEntity<TeamResponseModel> createTeam(@RequestBody TeamRequestModel teamRequestModel) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.createTeam(teamRequestModel));
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<TeamResponseModel> updateTeam(@PathVariable String teamId,
                                                        @RequestBody TeamRequestModel teamRequestModel) {
        if (teamId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid teamId provided: " + teamId);
        }
        return ResponseEntity
                .ok(teamService.updateTeam(teamRequestModel, teamId));    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(@PathVariable String teamId) {
        if (teamId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid teamId provided: " + teamId);
        }
        teamService.deleteTeam(teamId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
