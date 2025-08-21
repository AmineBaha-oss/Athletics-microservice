package com.athletics.apigateway.presentationlayer.competition;

import com.athletics.apigateway.businesslayer.competition.CompetitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1/teams/{teamId}/competitions")
public class CompetitionController {

    private final CompetitionService service;

    public CompetitionController(CompetitionService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CompetitionResponseModel>> getAllCompetitions(
            @PathVariable String teamId) {

        return ResponseEntity.ok(service.getAllCompetitions(teamId));
    }

    @GetMapping(value = "/{competitionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompetitionResponseModel> getCompetitionById(
            @PathVariable String teamId,
            @PathVariable String competitionId) {

        return ResponseEntity.ok(service.getCompetitionById(teamId, competitionId));
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompetitionResponseModel> createCompetition(
            @PathVariable String teamId,
            @RequestBody CompetitionRequestModel request) {

        CompetitionResponseModel created =
                service.createCompetition(teamId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping(
            value = "/{competitionId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompetitionResponseModel> updateCompetition(
            @PathVariable String teamId,
            @PathVariable String competitionId,
            @RequestBody CompetitionRequestModel request) {

        CompetitionResponseModel updated =
                service.updateCompetition(teamId, competitionId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{competitionId}")
    public ResponseEntity<Void> deleteCompetition(
            @PathVariable String teamId,
            @PathVariable String competitionId) {

        service.deleteCompetition(teamId, competitionId);
        return ResponseEntity.noContent().build();
    }
}
