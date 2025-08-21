package com.athletics.competition.domainclientLayer.team;

import com.athletics.competition.utils.CompetitionHttpErrorInfo;
import com.athletics.competition.utils.exceptions.InvalidInputException;
import com.athletics.competition.utils.exceptions.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Slf4j
@Component
public class TeamServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String TEAM_SERVICE_BASE_URL;

    public TeamServiceClient(RestTemplate restTemplate,
                             ObjectMapper mapper,
                             @Value("${app.team-service.host}") String host,
                             @Value("${app.team-service.port}") String port) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.TEAM_SERVICE_BASE_URL = "http://" + host + ":" + port + "/api/v1/teams";
    }

    public List<TeamModel> getAllTeams() {
        log.debug("Request received in API-Gateway Team Service Client: getAllTeams");
        try {
            String url = TEAM_SERVICE_BASE_URL;
            log.debug("Team-Service URL is: {}", url);
            ResponseEntity<TeamModel[]> responseEntity =
                    restTemplate.getForEntity(url, TeamModel[].class);
            TeamModel[] array = responseEntity.getBody();
            log.debug("Successfully retrieved teams");
            return array != null ? Arrays.asList(array) : new ArrayList<>();
        } catch (HttpClientErrorException ex) {
            log.error("Error Response Received in getAllTeams: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    public TeamModel getTeamByTeamId(String teamId) {
        log.debug("Request received in API-Gateway Team Service Client: getTeamByTeamId");
        try {
            String url = TEAM_SERVICE_BASE_URL + "/" + teamId;
            log.debug("Team-Service URL is: {}", url);
            String response = restTemplate.getForObject(url, String.class);
            log.debug("Successfully retrieved team JSON for id: {}", teamId);
            return ACLTeamModelFromJson(response);
        } catch (HttpClientErrorException ex) {
            log.error("Error Response Received in getTeamByTeamId: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private TeamModel ACLTeamModelFromJson(String response) throws JsonProcessingException {
        final JsonNode node        = mapper.readTree(response);
        final String teamId        = node.get("teamId").asText();
        final String teamName      = node.get("teamName").asText();
        final String coachName     = node.get("coachName").asText();
        final String level         = node.get("teamLevel").asText();

        TeamModel teamModel = TeamModel.builder()
                .teamId(teamId)
                .teamName(teamName)
                .coachName(coachName)
                .teamLevel(String.valueOf(TeamLevelEnum.valueOf(level.toUpperCase())))
                .build();

        return teamModel;
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        String errorMessage;
        try {
            errorMessage = mapper.readValue(ex.getResponseBodyAsString(), CompetitionHttpErrorInfo.class)
                    .getMessage();
        } catch (IOException ioEx) {
            errorMessage = ioEx.getMessage();
        }
        if (ex.getStatusCode() == NOT_FOUND) {
            return new NotFoundException(errorMessage);
        }
        if (ex.getStatusCode() == UNPROCESSABLE_ENTITY) {
            return new InvalidInputException(errorMessage);
        }
        log.warn("Unexpected HTTP error: {}, body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ex;
    }
}
