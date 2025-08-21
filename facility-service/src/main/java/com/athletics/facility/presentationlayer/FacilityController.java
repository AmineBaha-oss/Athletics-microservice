package com.athletics.facility.presentationlayer;

import com.athletics.facility.businesslayer.FacilityService;
import com.athletics.facility.presentationlayer.FacilityRequestModel;
import com.athletics.facility.presentationlayer.FacilityResponseModel;
import com.athletics.facility.utils.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/facilities")
public class FacilityController {

    private final FacilityService facilityService;
    private static final int UUID_LENGTH = 36;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @GetMapping
    public ResponseEntity<List<FacilityResponseModel>> getAllFacilities() {
        return ResponseEntity.ok().body(facilityService.getAllFacilities());
    }

    @GetMapping("/{facilityId}")
    public ResponseEntity<FacilityResponseModel> getFacilityById(@PathVariable String facilityId) {
        if (facilityId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid facilityId provided: " + facilityId);
        }
        return ResponseEntity.ok().body(facilityService.getFacilityById(facilityId));
    }

    @PostMapping
    public ResponseEntity<FacilityResponseModel> createFacility(@RequestBody FacilityRequestModel facilityRequestModel) {
        FacilityResponseModel newFacility = facilityService.createFacility(facilityRequestModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(newFacility);
    }

    @PutMapping("/{facilityId}")
    public ResponseEntity<FacilityResponseModel> updateFacility(
            @PathVariable String facilityId,
            @RequestBody FacilityRequestModel facilityRequestModel) {

        if (facilityId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid facilityId provided: " + facilityId);
        }

        FacilityResponseModel updatedFacility =
                facilityService.updateFacility(facilityRequestModel, facilityId);

        return ResponseEntity.ok(updatedFacility);
    }

    @DeleteMapping("/{facilityId}")
    public ResponseEntity<Void> deleteFacility(@PathVariable String facilityId) {
        if (facilityId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid facilityId provided: " + facilityId);
        }
        facilityService.deleteFacility(facilityId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
