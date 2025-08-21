package com.athletics.apigateway.domainclientlayer.competition;

import com.athletics.apigateway.presentationlayer.competition.CompetitionRequestModel;
import com.athletics.apigateway.presentationlayer.competition.CompetitionResponseModel;
import com.athletics.apigateway.utils.APIHttpErrorInfo;
import com.athletics.apigateway.utils.exceptions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.io.IOException;
import java.util.*;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@Component
public class CompetitionServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String COMPETITION_BASE_URL;

    public CompetitionServiceClient(RestTemplate restTemplate,
                                    ObjectMapper mapper,
                                    @Value("${app.competition-service.host}") String host,
                                    @Value("${app.competition-service.port}") String port) {
        this.restTemplate = restTemplate;
        this.mapper       = mapper;
        this.COMPETITION_BASE_URL =
                "http://" + host + ":" + port + "/api/v1/teams";
    }

    public List<CompetitionResponseModel> getAllCompetitions(String teamId) {
        log.debug("API-Gateway: Fetching all competitions for team={}", teamId);
        try {
            String url = COMPETITION_BASE_URL + "/" + teamId + "/competitions";
            ResponseEntity<CompetitionResponseModel[]> resp =
                    restTemplate.getForEntity(url, CompetitionResponseModel[].class);
            CompetitionResponseModel[] arr = resp.getBody();
            return arr != null ? Arrays.asList(arr) : List.of();
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public CompetitionResponseModel getCompetitionById(String teamId, String compId) {
        log.debug("API-Gateway: Fetching competition {} for team {}", compId, teamId);
        try {
            String url = COMPETITION_BASE_URL + "/" + teamId + "/competitions/" + compId;
            return restTemplate.getForObject(url, CompetitionResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public CompetitionResponseModel createCompetition(String teamId,
                                                      CompetitionRequestModel request) {
        log.debug("API-Gateway: Creating competition for team {}", teamId);
        try {
            String url = COMPETITION_BASE_URL + "/" + teamId + "/competitions";
            return restTemplate.postForObject(url, request, CompetitionResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public CompetitionResponseModel updateCompetition(String teamId,
                                                      String compId,
                                                      CompetitionRequestModel request) {
        log.debug("API-Gateway: Updating competition {} for team {}", compId, teamId);
        try {
            String url = COMPETITION_BASE_URL + "/" + teamId + "/competitions/" + compId;
            restTemplate.put(url, request);
            return getCompetitionById(teamId, compId);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public void deleteCompetition(String teamId, String compId) {
        log.debug("API-Gateway: Deleting competition {} for team {}", compId, teamId);
        try {
            String url = COMPETITION_BASE_URL + "/" + teamId + "/competitions/" + compId;
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    private String extractMessage(HttpClientErrorException ex) {
        try {
            APIHttpErrorInfo info = mapper.readValue(ex.getResponseBodyAsString(), APIHttpErrorInfo.class);
            return info.getMessage();
        } catch (IOException ioe) {
            return ex.getMessage();
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        String msg = extractMessage(ex);
        if (ex.getStatusCode() == NOT_FOUND) {
            return new NotFoundException(msg);
        }
        if (ex.getStatusCode() == UNPROCESSABLE_ENTITY) {
            return new InvalidInputException(msg);
        }
        log.warn("Unexpected HTTP error: {} – rethrowing", ex.getStatusCode());
        return ex;
    }
}
