package com.athletics.apigateway.domainclientlayer.team;

import com.athletics.apigateway.presentationlayer.team.TeamRequestModel;
import com.athletics.apigateway.presentationlayer.team.TeamResponseModel;
import com.athletics.apigateway.presentationlayer.team.AthleteRequestModel;
import com.athletics.apigateway.presentationlayer.team.AthleteResponseModel;
import com.athletics.apigateway.utils.APIHttpErrorInfo;
import com.athletics.apigateway.utils.exceptions.InvalidInputException;
import com.athletics.apigateway.utils.exceptions.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Slf4j
@Component
public class TeamServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String teamServiceHostAndPort;
    private final String TEAM_SERVICE_BASE_URL;

    public TeamServiceClient(RestTemplate restTemplate, ObjectMapper mapper,
                             @Value("${app.team-service.host}") String teamServiceHost,
                             @Value("${app.team-service.port}") String teamServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.teamServiceHostAndPort = teamServiceHost + ":" + teamServicePort;
        // Build the base URL for team endpoints.
        this.TEAM_SERVICE_BASE_URL = "http://" + teamServiceHostAndPort + "/api/v1/teams";
    }

    public List<TeamResponseModel> getAllTeams() {
        log.debug("Request received in API-Gateway Team Service Client: getAllTeams");
        try {
            String url = TEAM_SERVICE_BASE_URL;
            log.debug("Teams-Service URL is: " + url);
            ResponseEntity<TeamResponseModel[]> responseEntity =
                    restTemplate.getForEntity(url, TeamResponseModel[].class);
            TeamResponseModel[] teamsArray = responseEntity.getBody();
            log.debug("Successfully retrieved teams");
            return Arrays.asList(teamsArray);
        } catch (HttpClientErrorException ex) {
            log.debug("Error Response Received in getAllTeams");
            throw handleHttpClientException(ex);
        }
    }

    public TeamResponseModel getTeamById(String teamId) {
        log.debug("Request received in API-Gateway Team Service Client: getTeamById");
        try {
            String url = TEAM_SERVICE_BASE_URL + "/" + teamId;
            log.debug("Teams-Service URL is: " + url);
            TeamResponseModel teamResponseModel =
                    restTemplate.getForObject(url, TeamResponseModel.class);
            log.debug("Successfully retrieved team with id: " + teamResponseModel.getTeamId());
            return teamResponseModel;
        } catch (HttpClientErrorException ex) {
            log.debug("Error Response Received in getTeamById");
            throw handleHttpClientException(ex);
        }
    }

    public TeamResponseModel createTeam(TeamRequestModel team) {
        log.debug("Request received in API-Gateway Team Service Client: createTeam");
        try {
            String url = TEAM_SERVICE_BASE_URL;
            log.debug("Teams-Service URL for create is: " + url);
            TeamResponseModel createdTeam =
                    restTemplate.postForObject(url, team, TeamResponseModel.class);
            log.debug("Successfully created team with id: " + createdTeam.getTeamId());
            return createdTeam;
        } catch (HttpClientErrorException ex) {
            log.debug("Error Response Received in createTeam");
            throw handleHttpClientException(ex);
        }
    }

    public TeamResponseModel updateTeam(String teamId, TeamRequestModel team) {
        log.debug("Request received in API-Gateway Team Service Client: updateTeam");
        try {
            String url = TEAM_SERVICE_BASE_URL + "/" + teamId;
            log.debug("Teams-Service URL for update is: " + url);
            restTemplate.put(url, team);
            log.debug("Successfully updated team with id: " + teamId);
            return getTeamById(teamId);
        } catch (HttpClientErrorException ex) {
            log.debug("Error Response Received in updateTeam");
            throw handleHttpClientException(ex);
        }
    }

    public TeamResponseModel deleteTeam(String teamId) {
        log.debug("Request received in API-Gateway Team Service Client: deleteTeam");
        try {
            String url = TEAM_SERVICE_BASE_URL + "/" + teamId;
            log.debug("Teams-Service URL for delete is: " + url);
            restTemplate.delete(url);
            log.debug("Successfully deleted team with id: " + teamId);
        } catch (HttpClientErrorException ex) {
            log.debug("Error Response Received in deleteTeam");
            throw handleHttpClientException(ex);
        }
        return null;
    }

    public List<AthleteResponseModel> getAllAthletesForTeam(String teamId) {
        log.debug("Request received in API-Gateway Team Service Client: getAllAthletesForTeam");
        try {
            String url = "http://" + teamServiceHostAndPort + "/api/v1/" + teamId + "/athletes";
            log.debug("Athletes-Service URL is: " + url);
            ResponseEntity<AthleteResponseModel[]> responseEntity =
                    restTemplate.getForEntity(url, AthleteResponseModel[].class);
            AthleteResponseModel[] athletes = responseEntity.getBody();
            log.debug("Successfully retrieved athletes for team: " + teamId);
            return Arrays.asList(athletes);
        } catch (HttpClientErrorException ex) {
            log.debug("Error Response Received in getAllAthletesForTeam");
            throw handleHttpClientException(ex);
        }
    }

    public AthleteResponseModel getAthleteById(String teamId, String athleteId) {
        log.debug("Request received in API-Gateway Team Service Client: getAthleteById");
        try {
            String url = "http://" + teamServiceHostAndPort + "/api/v1/" + teamId + "/athletes/" + athleteId;
            log.debug("Athletes-Service URL is: " + url);
            AthleteResponseModel athlete = restTemplate.getForObject(url, AthleteResponseModel.class);
            log.debug("Successfully retrieved athlete with id: " + athlete.getAthleteId());
            return athlete;
        } catch (HttpClientErrorException ex) {
            log.debug("Error Response Received in getAthleteById");
            throw handleHttpClientException(ex);
        }
    }

    public AthleteResponseModel createAthleteForTeam(String teamId, AthleteRequestModel athlete) {
        log.debug("Request received in API-Gateway Team Service Client: createAthleteForTeam");
        try {
            String url = "http://" + teamServiceHostAndPort + "/api/v1/" + teamId + "/athletes";
            log.debug("Athletes-Service URL for create is: " + url);
            AthleteResponseModel createdAthlete =
                    restTemplate.postForObject(url, athlete, AthleteResponseModel.class);
            log.debug("Successfully created athlete with id: " + createdAthlete.getAthleteId());
            return createdAthlete;
        } catch (HttpClientErrorException ex) {
            log.debug("Error Response Received in createAthleteForTeam");
            throw handleHttpClientException(ex);
        }
    }

    public AthleteResponseModel updateAthleteForTeam(String teamId, String athleteId, AthleteRequestModel athlete) {
        log.debug("Request received in API-Gateway Team Service Client: updateAthleteForTeam");
        try {
            String url = "http://" + teamServiceHostAndPort + "/api/v1/" + teamId + "/athletes/" + athleteId;
            log.debug("Athletes-Service URL for update is: " + url);
            restTemplate.put(url, athlete);
            log.debug("Successfully updated athlete with id: " + athleteId);
            return getAthleteById(teamId, athleteId);
        } catch (HttpClientErrorException ex) {
            log.debug("Error Response Received in updateAthleteForTeam");
            throw handleHttpClientException(ex);
        }
    }

    public void deleteAthleteForTeam(String teamId, String athleteId) {
        log.debug("Request received in API-Gateway Team Service Client: deleteAthleteForTeam");
        try {
            String url = "http://" + teamServiceHostAndPort + "/api/v1/" + teamId + "/athletes/" + athleteId;
            log.debug("Athletes-Service URL for delete is: " + url);
            restTemplate.delete(url);
            log.debug("Successfully deleted athlete with id: " + athleteId);
        } catch (HttpClientErrorException ex) {
            log.debug("Error Response Received in deleteAthleteForTeam");
            throw handleHttpClientException(ex);
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), APIHttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ioex.getMessage();
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        if (ex.getStatusCode() == NOT_FOUND) {
            return new NotFoundException(getErrorMessage(ex));
        }
        if (ex.getStatusCode() == UNPROCESSABLE_ENTITY) {
            return new InvalidInputException(getErrorMessage(ex));
        }
        log.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
        log.warn("Error body: {}", ex.getResponseBodyAsString());
        return ex;
    }
}
