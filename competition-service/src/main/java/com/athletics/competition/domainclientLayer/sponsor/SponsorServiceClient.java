package com.athletics.competition.domainclientLayer.sponsor;

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
public class SponsorServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String SPONSOR_BASE_URL;

    public SponsorServiceClient(RestTemplate restTemplate,
                                ObjectMapper mapper,
                                @Value("${app.sponsor-service.host}") String host,
                                @Value("${app.sponsor-service.port}") String port) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.SPONSOR_BASE_URL = "http://" + host + ":" + port + "/api/v1/sponsors";
    }

    public List<SponsorModel> getAllSponsors() {
        log.debug("Request received in API-Gateway Sponsor Service Client: getAllSponsors");
        try {
            String url = SPONSOR_BASE_URL;
            log.debug("Sponsor-Service URL is: {}", url);
            ResponseEntity<SponsorModel[]> resp =
                    restTemplate.getForEntity(url, SponsorModel[].class);
            SponsorModel[] array = resp.getBody();
            log.debug("Successfully retrieved sponsors");
            return array != null ? Arrays.asList(array) : new ArrayList<>();
        } catch (HttpClientErrorException ex) {
            log.error("Error Response Received in getAllSponsors: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    public SponsorModel getSponsorBySponsorId(String sponsorId) {
        log.debug("Request received in API-Gateway Sponsor Service Client: getSponsorBySponsorId");
        try {
            String url = SPONSOR_BASE_URL + "/" + sponsorId;
            log.debug("Sponsor-Service URL is: {}", url);
            String response = restTemplate.getForObject(url, String.class);
            log.debug("Successfully retrieved sponsor JSON for id: {}", sponsorId);
            return ACLSponsorModelFromJson(response);
        } catch (HttpClientErrorException ex) {
            log.error("Error Response Received in getSponsorBySponsorId: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public SponsorModel patchSponsorLevelBySponsorId(String sponsorId, String newLevel) {
        log.debug("Request received in API-Gateway Sponsor Service Client: patchSponsorLevel");
        try {
            String url = SPONSOR_BASE_URL + "/" + sponsorId + "/level";
            log.debug("Sponsor-Service URL is: {}", url);
            String response = restTemplate.patchForObject(url, newLevel, String.class);
            log.debug("Successfully patched sponsor level for id: {}", sponsorId);
            return ACLSponsorModelFromJson(response);
        } catch (HttpClientErrorException ex) {
            log.error("Error Response Received in patchSponsorLevel: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private SponsorModel ACLSponsorModelFromJson(String response) throws JsonProcessingException {
        final JsonNode node         = mapper.readTree(response);
        final String sponsorId      = node.get("sponsorId").asText();
        final String sponsorName    = node.get("sponsorName").asText();
        final double sponsorAmount  = node.get("sponsorAmount").asDouble();
        final String sponsorLevel   = node.get("sponsorLevel").asText();

        SponsorModel sponsorModel = SponsorModel.builder()
                .sponsorId(   sponsorId)
                .sponsorName( sponsorName)
                .sponsorAmount(sponsorAmount)
                .sponsorLevel(String.valueOf(SponsorLevelEnum.valueOf(sponsorLevel.toUpperCase())))
                .build();

        return sponsorModel;
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
