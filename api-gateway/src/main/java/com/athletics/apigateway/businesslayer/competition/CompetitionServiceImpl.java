package com.athletics.apigateway.businesslayer.competition;


import com.athletics.apigateway.domainclientlayer.competition.CompetitionServiceClient;
import com.athletics.apigateway.presentationlayer.competition.CompetitionController;
import com.athletics.apigateway.presentationlayer.competition.CompetitionRequestModel;
import com.athletics.apigateway.presentationlayer.competition.CompetitionResponseModel;
import com.athletics.apigateway.utils.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@Service
public class CompetitionServiceImpl implements CompetitionService {

    private static final int UUID_LEN = 36;
    private final CompetitionServiceClient client;

    public CompetitionServiceImpl(CompetitionServiceClient client) {
        this.client = client;
    }

    @Override
    public List<CompetitionResponseModel> getAllCompetitions(String teamId) {
        validateUuid(teamId, "teamId");
        List<CompetitionResponseModel> list = client.getAllCompetitions(teamId);
        list.forEach(this::addLinks);
        return list;
    }

    @Override
    public CompetitionResponseModel getCompetitionById(String teamId, String compId) {
        validateUuid(teamId, "teamId");
        validateUuid(compId, "competitionId");
        CompetitionResponseModel dto = client.getCompetitionById(teamId, compId);
        addLinks(dto);
        return dto;
    }

    @Override
    public CompetitionResponseModel createCompetition(String teamId, CompetitionRequestModel request) {
        validateUuid(teamId, "teamId");
        CompetitionResponseModel created = client.createCompetition(teamId, request);
        addLinks(created);
        return created;
    }

    @Override
    public CompetitionResponseModel updateCompetition(String teamId, String compId, CompetitionRequestModel request) {
        validateUuid(teamId, "teamId");
        validateUuid(compId, "competitionId");
        CompetitionResponseModel updated = client.updateCompetition(teamId, compId, request);
        addLinks(updated);
        return updated;
    }

    @Override
    public void deleteCompetition(String teamId, String compId) {
        validateUuid(teamId, "teamId");
        validateUuid(compId, "competitionId");
        client.deleteCompetition(teamId, compId);
    }

    private void addLinks(CompetitionResponseModel c) {
        if (c == null || c.getCompetitionId() == null) return;

        c.add(linkTo(methodOn(CompetitionController.class)
                .getCompetitionById(c.getTeamId(), c.getCompetitionId()))
                .withSelfRel());

        c.add(linkTo(methodOn(CompetitionController.class)
                .getAllCompetitions(c.getTeamId()))
                .withRel("competitions"));
    }

    private void validateUuid(String id, String name) {
        try {
            if (id.length() != UUID_LEN) throw new IllegalArgumentException();
            UUID.fromString(id);
        } catch (Exception ex) {
            throw new InvalidInputException("Invalid " + name + ": " + id);
        }
    }
}
