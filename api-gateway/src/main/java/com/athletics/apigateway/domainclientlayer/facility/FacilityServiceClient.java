package com.athletics.apigateway.domainclientlayer.facility;



import com.athletics.apigateway.presentationlayer.facility.FacilityRequestModel;
import com.athletics.apigateway.presentationlayer.facility.FacilityResponseModel;
import com.athletics.apigateway.utils.APIHttpErrorInfo;
import com.athletics.apigateway.utils.exceptions.InvalidInputException;
import com.athletics.apigateway.utils.exceptions.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
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
public class FacilityServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String FACILITY_SERVICE_BASE_URL;

    public FacilityServiceClient(RestTemplate restTemplate,
                                 ObjectMapper mapper,
                                 @Value("${app.facility-service.host}") String facilityServiceHost,
                                 @Value("${app.facility-service.port}") String facilityServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        FACILITY_SERVICE_BASE_URL =
                "http://" + facilityServiceHost + ":" + facilityServicePort + "/api/v1/facilities";
    }

    public List<FacilityResponseModel> getAllFacilities() {
        log.debug("2. Request received in API-Gateway FacilityServiceClient: getAllFacilities()");
        try {
            String url = FACILITY_SERVICE_BASE_URL;
            log.debug("Facility-Service URL is: {}", url);

            ResponseEntity<FacilityResponseModel[]> responseEntity =
                    restTemplate.getForEntity(url, FacilityResponseModel[].class);
            FacilityResponseModel[] facilityArray = responseEntity.getBody();

            log.debug("4. Successfully retrieved facilities from Facility-Service");
            return Arrays.asList(facilityArray);
        } catch (HttpClientErrorException ex) {
            log.debug("5. Error Response Received in getAllFacilities()");
            throw handleHttpClientException(ex);
        }
    }

    public FacilityResponseModel getFacilityById(String facilityId) {
        log.debug("2. Request received in API-Gateway FacilityServiceClient: getFacilityById({})", facilityId);
        try {
            String url = FACILITY_SERVICE_BASE_URL + "/" + facilityId;
            log.debug("Facility-Service URL is: {}", url);

            FacilityResponseModel facilityResponse =
                    restTemplate.getForObject(url, FacilityResponseModel.class);

            log.debug("4. Successfully retrieved facility with ID: {}", facilityResponse.getFacilityId());
            return facilityResponse;
        } catch (HttpClientErrorException ex) {
            log.debug("5. Error Response Received in getFacilityById({})", facilityId);
            throw handleHttpClientException(ex);
        }
    }

    public FacilityResponseModel createFacility(FacilityRequestModel facilityRequestModel) {
        log.debug("2. Request received in API-Gateway FacilityServiceClient: createFacility()");
        try {
            String url = FACILITY_SERVICE_BASE_URL;
            log.debug("Facility-Service URL for create is: {}", url);

            FacilityResponseModel createdFacility =
                    restTemplate.postForObject(url, facilityRequestModel, FacilityResponseModel.class);

            log.debug("4. Successfully created facility with ID: {}",
                    createdFacility.getFacilityId());
            return createdFacility;
        } catch (HttpClientErrorException ex) {
            log.debug("5. Error Response Received in createFacility()");
            throw handleHttpClientException(ex);
        }
    }

    public FacilityResponseModel updateFacility(String facilityId, FacilityRequestModel facilityRequestModel) {
        log.debug("2. Request received in API-Gateway FacilityServiceClient: updateFacility({})", facilityId);
        try {
            String url = FACILITY_SERVICE_BASE_URL + "/" + facilityId;
            log.debug("Facility-Service URL for update is: {}", url);

            // PUT does not return a resource body
            restTemplate.put(url, facilityRequestModel);

            log.debug("4. Successfully updated facility with ID: {}", facilityId);
            // Optionally retrieve the updated resource
            return getFacilityById(facilityId);
        } catch (HttpClientErrorException ex) {
            log.debug("5. Error Response Received in updateFacility({})", facilityId);
            throw handleHttpClientException(ex);
        }
    }

    public void deleteFacility(String facilityId) {
        log.debug("2. Request received in API-Gateway FacilityServiceClient: deleteFacility({})", facilityId);
        try {
            String url = FACILITY_SERVICE_BASE_URL + "/" + facilityId;
            log.debug("Facility-Service URL for delete is: {}", url);

            restTemplate.delete(url);
            log.debug("4. Successfully deleted facility with ID: {}", facilityId);
        } catch (HttpClientErrorException ex) {
            log.debug("5. Error Response Received in deleteFacility({})", facilityId);
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
        HttpStatusCode statusCode = ex.getStatusCode();
        if (statusCode.equals(NOT_FOUND)) {
            return new NotFoundException(getErrorMessage(ex));
        } else if (statusCode.equals(UNPROCESSABLE_ENTITY)) {
            return new InvalidInputException(getErrorMessage(ex));
        }
        log.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
        log.warn("Error body: {}", ex.getResponseBodyAsString());
        return ex;
    }
}
