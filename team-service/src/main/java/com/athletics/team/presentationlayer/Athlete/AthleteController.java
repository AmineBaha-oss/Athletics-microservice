package com.athletics.team.presentationlayer.Athlete;

import com.athletics.team.businesslayer.Athlete.AthleteService;
import com.athletics.team.presentationlayer.Athlete.AthleteRequestModel;
import com.athletics.team.presentationlayer.Athlete.AthleteResponseModel;
import com.athletics.team.utils.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/{teamId}/athletes")
public class AthleteController {

    private final AthleteService athleteService;
    private static final int UUID_LENGTH = 36;

    public AthleteController(AthleteService athleteService) {
        this.athleteService = athleteService;
    }

    @GetMapping
    public ResponseEntity<List<AthleteResponseModel>> getAthletesByTeam(@PathVariable String teamId) {
        if (teamId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid teamId provided: " + teamId);
        }
        List<AthleteResponseModel> athletes = athleteService.getAllAthletesForTeam(teamId);
        return ResponseEntity.ok(athletes);
    }

    @GetMapping("/{athleteId}")
    public ResponseEntity<AthleteResponseModel> getAthleteById(@PathVariable String teamId,
                                                               @PathVariable String athleteId) {
        if (teamId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid teamId provided: " + teamId);
        }
        if (athleteId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid athleteId provided: " + athleteId);
        }
        AthleteResponseModel athlete = athleteService.getAthleteById(teamId, athleteId);
        return ResponseEntity.ok(athlete);
    }

    @PostMapping
    public ResponseEntity<AthleteResponseModel> createAthlete(@PathVariable String teamId,
                                                              @RequestBody AthleteRequestModel athleteRequestModel) {
        if (teamId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid teamId provided: " + teamId);
        }
        AthleteResponseModel createdAthlete = athleteService.createAthleteForTeam(teamId, athleteRequestModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAthlete);
    }

    @PutMapping("/{athleteId}")
    public ResponseEntity<AthleteResponseModel> updateAthlete(@PathVariable String teamId,
                                                              @PathVariable String athleteId,
                                                              @RequestBody AthleteRequestModel athleteRequestModel) {
        if (teamId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid teamId provided: " + teamId);
        }
        if (athleteId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid athleteId provided: " + athleteId);
        }
        AthleteResponseModel updatedAthlete =
                athleteService.updateAthleteForTeam(teamId, athleteId, athleteRequestModel);
        return ResponseEntity.ok(updatedAthlete);
    }

    @DeleteMapping("/{athleteId}")
    public ResponseEntity<Void> deleteAthlete(@PathVariable String teamId,
                                              @PathVariable String athleteId) {
        if (teamId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid teamId provided: " + teamId);
        }
        if (athleteId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid athleteId provided: " + athleteId);
        }
        athleteService.deleteAthleteForTeam(teamId, athleteId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
