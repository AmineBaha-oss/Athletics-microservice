package com.athletics.competition.domainclientLayer.facility;

import com.athletics.competition.utils.CompetitionHttpErrorInfo;
import com.athletics.competition.utils.exceptions.InvalidInputException;
import com.athletics.competition.utils.exceptions.NotFoundException;
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
public class FacilityServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String FACILITY_SERVICE_BASE_URL;

    public FacilityServiceClient(RestTemplate restTemplate,
                                 ObjectMapper mapper,
                                 @Value("${app.facility-service.host}") String host,
                                 @Value("${app.facility-service.port}") String port) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.FACILITY_SERVICE_BASE_URL = "http://" + host + ":" + port + "/api/v1/facilities";
    }

    public List<FacilityModel> getAllFacilities() {
        log.debug("2. Request received in API-Gateway Facility Service Client: getAllFacilities");
        try {
            String url = FACILITY_SERVICE_BASE_URL;
            log.debug("3. Facility-Service URL is: {}", url);
            ResponseEntity<FacilityModel[]> responseEntity =
                    restTemplate.getForEntity(url, FacilityModel[].class);
            FacilityModel[] array = responseEntity.getBody();
            log.debug("4. Successfully retrieved facilities");
            return array != null ? Arrays.asList(array) : new ArrayList<>();
        } catch (HttpClientErrorException ex) {
            log.debug("5. Error Response Received in getAllFacilities: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    public FacilityModel getFacilityByFacilityId(String facilityId) {
        log.debug("2. Request received in API-Gateway Facility Service Client: getFacilityByFacilityId");
        try {
            String url = FACILITY_SERVICE_BASE_URL + "/" + facilityId;
            log.debug("3. Facility-Service URL is: {}", url);
            FacilityModel model = restTemplate.getForObject(url, FacilityModel.class);
            log.debug("4. Successfully retrieved facility with id: {}", facilityId);
            return model;
        } catch (HttpClientErrorException ex) {
            log.debug("5. Error Response Received in getFacilityByFacilityId: {}", ex.getStatusCode());
            throw handleHttpClientException(ex);
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), CompetitionHttpErrorInfo.class)
                    .getMessage();
        } catch (IOException ioex) {
            return ioex.getMessage();
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        String msg = getErrorMessage(ex);
        if (ex.getStatusCode() == NOT_FOUND) {
            return new NotFoundException(msg);
        }
        if (ex.getStatusCode() == UNPROCESSABLE_ENTITY) {
            return new InvalidInputException(msg);
        }
        log.warn("Got an unexpected HTTP error: {}, body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ex;
    }
}
