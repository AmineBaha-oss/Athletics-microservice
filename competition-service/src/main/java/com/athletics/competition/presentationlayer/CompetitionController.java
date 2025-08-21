package com.athletics.competition.presentationlayer;


import com.athletics.competition.businesslayer.CompetitionService;
import com.athletics.competition.utils.exceptions.InvalidInputException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams/{teamId}/competitions")
public class CompetitionController {

    private final CompetitionService competitionService;
    private static final int UUID_LENGTH = 36;

    public CompetitionController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    @GetMapping
    public ResponseEntity<List<CompetitionResponseModel>> getAllCompetitions(
            @PathVariable String teamId) {

        if (teamId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid teamId provided: " + teamId);
        }
        List<CompetitionResponseModel> list = competitionService.getAllCompetitions(teamId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{competitionId}")
    public ResponseEntity<CompetitionResponseModel> getCompetitionById(
            @PathVariable String teamId,
            @PathVariable String competitionId) {

        if (teamId.length() != UUID_LENGTH || competitionId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid ID provided");
        }
        CompetitionResponseModel dto = competitionService.getCompetitionById(teamId, competitionId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<CompetitionResponseModel> createCompetition(
            @PathVariable String teamId,
            @RequestBody CompetitionRequestModel request) {

        if (teamId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid teamId provided: " + teamId);
        }
        CompetitionResponseModel created = competitionService.createCompetition(teamId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{competitionId}")
    public ResponseEntity<CompetitionResponseModel> updateCompetition(
            @PathVariable String teamId,
            @PathVariable String competitionId,
            @RequestBody CompetitionRequestModel request) {

        if (teamId.length() != UUID_LENGTH || competitionId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid ID provided");
        }
        CompetitionResponseModel updated =
                competitionService.updateCompetition(teamId, competitionId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{competitionId}")
    public ResponseEntity<Void> deleteCompetition(
            @PathVariable String teamId,
            @PathVariable String competitionId) {

        if (teamId.length() != UUID_LENGTH || competitionId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid ID provided");
        }
        competitionService.deleteCompetition(teamId, competitionId);
        return ResponseEntity.noContent().build();
    }
}
