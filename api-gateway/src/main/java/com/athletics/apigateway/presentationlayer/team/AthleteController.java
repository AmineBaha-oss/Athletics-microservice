package com.athletics.apigateway.presentationlayer.team;


import com.athletics.apigateway.businesslayer.team.TeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@RestController
@RequestMapping("api/v1/teams/{teamId}/athletes")
public class AthleteController {

    private final TeamService teamService;

    public AthleteController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public ResponseEntity<List<AthleteResponseModel>> getAllAthletes(@PathVariable String teamId) {
        log.debug("Presentation Layer: getAllAthletes() called for team {}", teamId);
        List<AthleteResponseModel> athletes = teamService.getAllAthletes(teamId);

        return ResponseEntity.ok(athletes);
    }

    @GetMapping("/{athleteId}")
    public ResponseEntity<AthleteResponseModel> getAthleteById(@PathVariable String teamId,
                                                               @PathVariable String athleteId) {
        log.debug("Presentation Layer: getAthleteById({}, {}) called", teamId, athleteId);
        AthleteResponseModel athlete = teamService.getAthleteById(teamId, athleteId);

        return ResponseEntity.ok(athlete);
    }

    @PostMapping
    public ResponseEntity<AthleteResponseModel> createAthlete(@PathVariable String teamId,
                                                              @RequestBody AthleteRequestModel athleteRequestModel) {
        log.debug("Presentation Layer: createAthlete() called for team {}", teamId);
        AthleteResponseModel newAthlete = teamService.createAthlete(teamId, athleteRequestModel);

        return ResponseEntity.status(HttpStatus.CREATED).body(newAthlete);
    }

    @PutMapping("/{athleteId}")
    public ResponseEntity<AthleteResponseModel> updateAthlete(@PathVariable String teamId,
                                                              @PathVariable String athleteId,
                                                              @RequestBody AthleteRequestModel athleteRequestModel) {
        log.debug("Presentation Layer: updateAthlete({}, {}) called", teamId, athleteId);
        AthleteResponseModel updatedAthlete = teamService.updateAthlete(teamId, athleteId, athleteRequestModel);

        return ResponseEntity.ok(updatedAthlete);
    }

    @DeleteMapping("/{athleteId}")
    public ResponseEntity<Void> deleteAthlete(@PathVariable String teamId,
                                              @PathVariable String athleteId) {
        log.debug("Presentation Layer: deleteAthlete({}, {}) called", teamId, athleteId);
        teamService.deleteAthlete(teamId, athleteId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
