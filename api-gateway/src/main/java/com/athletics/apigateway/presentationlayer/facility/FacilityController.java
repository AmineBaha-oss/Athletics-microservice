package com.athletics.apigateway.presentationlayer.facility;

import com.athletics.apigateway.businesslayer.facility.FacilityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@Slf4j
@RequestMapping("api/v1/facilities")
public class FacilityController {

    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }


    @GetMapping(produces = "application/json")
    public ResponseEntity<List<FacilityResponseModel>> getAllFacilities() {
        log.debug("Presentation Layer: getAllFacilities() called");
        List<FacilityResponseModel> facilities = facilityService.getAllFacilities();


        return ResponseEntity.ok(facilities);
    }


    @GetMapping(value = "/{facilityId}", produces = "application/json")
    public ResponseEntity<FacilityResponseModel> getFacilityById(@PathVariable String facilityId) {
        log.debug("Presentation Layer: getFacilityById({}) called", facilityId);
        FacilityResponseModel facility = facilityService.getFacilityById(facilityId);

        return ResponseEntity.ok(facility);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<FacilityResponseModel> createFacility(@RequestBody FacilityRequestModel facilityRequestModel) {
        log.debug("Presentation Layer: createFacility() called");
        FacilityResponseModel createdFacility = facilityService.createFacility(facilityRequestModel);


        return ResponseEntity.status(HttpStatus.CREATED).body(createdFacility);
    }


    @PutMapping(value = "/{facilityId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<FacilityResponseModel> updateFacility(@PathVariable String facilityId,
                                                                @RequestBody FacilityRequestModel facilityRequestModel) {
        log.debug("Presentation Layer: updateFacility({}) called", facilityId);
        FacilityResponseModel updatedFacility = facilityService.updateFacility(facilityId, facilityRequestModel);


        return ResponseEntity.ok(updatedFacility);
    }


    @DeleteMapping(value = "/{facilityId}")
    public ResponseEntity<Void> deleteFacility(@PathVariable String facilityId) {
        log.debug("Presentation Layer: deleteFacility({}) called", facilityId);
        facilityService.deleteFacility(facilityId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
