package com.athletics.apigateway.businesslayer.facility;

import com.athletics.apigateway.domainclientlayer.facility.FacilityServiceClient;
import com.athletics.apigateway.presentationlayer.facility.FacilityController;
import com.athletics.apigateway.presentationlayer.facility.FacilityRequestModel;
import com.athletics.apigateway.presentationlayer.facility.FacilityResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@Service
public class FacilityServiceImpl implements FacilityService {

    private final FacilityServiceClient facilityServiceClient;

    public FacilityServiceImpl(FacilityServiceClient facilityServiceClient) {
        this.facilityServiceClient = facilityServiceClient;
    }

    @Override
    public List<FacilityResponseModel> getAllFacilities() {
        log.debug("1. Received request in API-Gateway FacilityServiceImpl: getAllFacilities()");
        List<FacilityResponseModel> facilities = facilityServiceClient.getAllFacilities();

        for (FacilityResponseModel facility : facilities) {
            addHateoasLinks(facility);
        }
        return facilities;
    }

    @Override
    public FacilityResponseModel getFacilityById(String facilityId) {
        log.debug("1. Received request in API-Gateway FacilityServiceImpl: getFacilityById({})", facilityId);
        FacilityResponseModel facility = facilityServiceClient.getFacilityById(facilityId);
        return addHateoasLinks(facility);
    }

    @Override
    public FacilityResponseModel createFacility(FacilityRequestModel facilityRequestModel) {
        log.debug("1. Received request in API-Gateway FacilityServiceImpl: createFacility()");
        FacilityResponseModel createdFacility = facilityServiceClient.createFacility(facilityRequestModel);
        return addHateoasLinks(createdFacility);
    }

    @Override
    public FacilityResponseModel updateFacility(String facilityId, FacilityRequestModel facilityRequestModel) {
        log.debug("1. Received request in API-Gateway FacilityServiceImpl: updateFacility({})", facilityId);
        FacilityResponseModel updatedFacility = facilityServiceClient.updateFacility(facilityId, facilityRequestModel);
        return addHateoasLinks(updatedFacility);
    }

    @Override
    public void deleteFacility(String facilityId) {
        log.debug("1. Received request in API-Gateway FacilityServiceImpl: deleteFacility({})", facilityId);
        facilityServiceClient.deleteFacility(facilityId);
    }


    private FacilityResponseModel addHateoasLinks(FacilityResponseModel facility) {
        Link selfLink = linkTo(methodOn(FacilityController.class)
                .getFacilityById(facility.getFacilityId()))
                .withSelfRel();
        facility.add(selfLink);

        Link allFacilitiesLink = linkTo(methodOn(FacilityController.class)
                .getAllFacilities())
                .withRel("all-facilities");
        facility.add(allFacilitiesLink);

        return facility;
    }
}
