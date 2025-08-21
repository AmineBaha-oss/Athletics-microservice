package com.athletics.apigateway.domainclientlayer.sponsor;

import com.athletics.apigateway.presentationlayer.sponsor.SponsorRequestModel;
import com.athletics.apigateway.presentationlayer.sponsor.SponsorResponseModel;
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
    private final String SPONSOR_SERVICE_BASE_URL;

    public SponsorServiceClient(RestTemplate restTemplate, ObjectMapper mapper,
                                @Value("${app.sponsor-service.host}") String sponsorServiceHost,
                                @Value("${app.sponsor-service.port}") String sponsorServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.SPONSOR_SERVICE_BASE_URL = "http://" + sponsorServiceHost + ":" + sponsorServicePort + "/api/v1/sponsors";
    }

    public List<SponsorResponseModel> getAllSponsors() {
        log.debug("2. Request received in API-Gateway Sponsor Service Client: getAllSponsors");
        try {
            String url = SPONSOR_SERVICE_BASE_URL;
            log.debug("Sponsor-Service URL is: " + url);
            ResponseEntity<SponsorResponseModel[]> response =
                    restTemplate.getForEntity(url, SponsorResponseModel[].class);
            SponsorResponseModel[] sponsorArray = response.getBody();
            log.debug("4. Successfully retrieved sponsors");
            return Arrays.asList(sponsorArray);
        } catch (HttpClientErrorException ex) {
            log.debug("5. Error Response Received in getAllSponsors");
            throw handleHttpClientException(ex);
        }
    }

    public SponsorResponseModel getSponsorById(String sponsorId) {
        log.debug("2. Request received in API-Gateway Sponsor Service Client: getSponsorById");
        try {
            String url = SPONSOR_SERVICE_BASE_URL + "/" + sponsorId;
            log.debug("Sponsor-Service URL is: " + url);
            SponsorResponseModel sponsor =
                    restTemplate.getForObject(url, SponsorResponseModel.class);
            log.debug("4. Successfully retrieved sponsor with id: {}", sponsor.getSponsorId());
            return sponsor;
        } catch (HttpClientErrorException ex) {
            log.debug("5. Error Response Received in getSponsorById");
            throw handleHttpClientException(ex);
        }
    }

    public SponsorResponseModel createSponsor(SponsorRequestModel sponsorRequest) {
        log.debug("2. Request received in API-Gateway Sponsor Service Client: createSponsor");
        try {
            String url = SPONSOR_SERVICE_BASE_URL;
            log.debug("Sponsor-Service URL for create is: " + url);
            SponsorResponseModel sponsor =
                    restTemplate.postForObject(url, sponsorRequest, SponsorResponseModel.class);
            log.debug("4. Successfully created sponsor with id: {}", sponsor.getSponsorId());
            return sponsor;
        } catch (HttpClientErrorException ex) {
            log.debug("5. Error Response Received in createSponsor");
            throw handleHttpClientException(ex);
        }
    }

    public SponsorResponseModel updateSponsor(String sponsorId, SponsorRequestModel sponsorRequest) {
        log.debug("2. Request received in API-Gateway Sponsor Service Client: updateSponsor");
        try {
            String url = SPONSOR_SERVICE_BASE_URL + "/" + sponsorId;
            log.debug("Sponsor-Service URL for update is: " + url);
            restTemplate.put(url, sponsorRequest);
            log.debug("4. Successfully updated sponsor with id: {}", sponsorId);
            return getSponsorById(sponsorId);
        } catch (HttpClientErrorException ex) {
            log.debug("5. Error Response Received in updateSponsor");
            throw handleHttpClientException(ex);
        }
    }

    public SponsorResponseModel deleteSponsor(String sponsorId) {
        log.debug("2. Request received in API-Gateway Sponsor Service Client: deleteSponsor");
        try {
            String url = SPONSOR_SERVICE_BASE_URL + "/" + sponsorId;
            log.debug("Sponsor-Service URL for delete is: " + url);
            restTemplate.delete(url);
            log.debug("4. Successfully deleted sponsor with id: {}", sponsorId);
        } catch (HttpClientErrorException ex) {
            log.debug("5. Error Response Received in deleteSponsor");
            throw handleHttpClientException(ex);
        }
        return null;
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
