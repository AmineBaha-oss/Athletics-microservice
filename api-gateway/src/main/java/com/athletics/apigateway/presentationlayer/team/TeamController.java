package com.athletics.apigateway.presentationlayer.team;


import com.athletics.apigateway.businesslayer.team.TeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@Slf4j
@RequestMapping("api/v1/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public ResponseEntity<List<TeamResponseModel>> getAllTeams() {
        log.debug("Presentation Layer: getAllTeams() called");
        List<TeamResponseModel> teams = teamService.getAllTeams();

        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponseModel> getTeamById(@PathVariable String teamId) {
        log.debug("Presentation Layer: getTeamById({}) called", teamId);
        TeamResponseModel team = teamService.getTeamById(teamId);

        return ResponseEntity.ok(team);
    }

    @PostMapping
    public ResponseEntity<TeamResponseModel> createTeam(@RequestBody TeamRequestModel teamRequestModel) {
        log.debug("Presentation Layer: createTeam() called");
        TeamResponseModel newTeam = teamService.createTeam(teamRequestModel);

        return ResponseEntity.status(HttpStatus.CREATED).body(newTeam);
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<TeamResponseModel> updateTeam(@PathVariable String teamId,
                                                        @RequestBody TeamRequestModel teamRequestModel) {
        log.debug("Presentation Layer: updateTeam({}) called", teamId);
        TeamResponseModel updatedTeam = teamService.updateTeam(teamId, teamRequestModel);

        return ResponseEntity.ok(updatedTeam);

    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(@PathVariable String teamId) {
        log.debug("Presentation Layer: deleteTeam({}) called", teamId);
        teamService.deleteTeam(teamId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
