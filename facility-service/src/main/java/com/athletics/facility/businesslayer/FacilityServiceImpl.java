package com.athletics.facility.businesslayer;



import com.athletics.facility.dataaccesslayer.Facility;
import com.athletics.facility.dataaccesslayer.FacilityIdentifier;
import com.athletics.facility.dataaccesslayer.FacilityRepository;
import com.athletics.facility.mappinglayer.FacilityResponseMapper;
import com.athletics.facility.mappinglayer.FacilityRequestMapper;

import com.athletics.facility.presentationlayer.FacilityRequestModel;
import com.athletics.facility.presentationlayer.FacilityResponseModel;
import com.athletics.facility.utils.exceptions.InsufficientFacilityCapacityException;
import com.athletics.facility.utils.exceptions.InvalidInputException;
import com.athletics.facility.utils.exceptions.NotFoundException;

import java.util.List;

@Service
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;
    private final FacilityResponseMapper facilityResponseMapper;
    private final FacilityRequestMapper facilityRequestMapper;

    public FacilityServiceImpl(FacilityRepository facilityRepository,
                               FacilityResponseMapper facilityResponseMapper,
                               FacilityRequestMapper facilityRequestMapper) {
        this.facilityRepository = facilityRepository;
        this.facilityResponseMapper = facilityResponseMapper;
        this.facilityRequestMapper = facilityRequestMapper;
    }

    @Override
    public List<FacilityResponseModel> getAllFacilities() {
        return facilityResponseMapper.entityListToResponseModelList(facilityRepository.findAll());
    }

    @Override
    public FacilityResponseModel getFacilityById(String facilityId) {
        Facility facility = facilityRepository.findByFacilityIdentifier_FacilityId(facilityId);
        if (facility == null) {
            throw new NotFoundException("Facility not found with ID: " + facilityId);
        }
        return facilityResponseMapper.entityToResponseModel(facility);
    }

    @Override
    public FacilityResponseModel createFacility(FacilityRequestModel facilityRequestModel) {
        if (facilityRepository.findByFacilityIdentifier_FacilityId(facilityRequestModel.getFacilityId()) != null) {
            throw new InvalidInputException("Facility with ID already exists: " + facilityRequestModel.getFacilityId());
        }

        int minimumCapacity = 50;
        if (facilityRequestModel.getCapacity() != null && facilityRequestModel.getCapacity() < minimumCapacity) {
            throw new InsufficientFacilityCapacityException(
                    "The facility capacity is too low. A minimum capacity of " + minimumCapacity + " is required."
            );
        }

        Facility newFacility = facilityRequestMapper.requestModelToEntity(
                facilityRequestModel,
                new FacilityIdentifier(facilityRequestModel.getFacilityId())
        );

        Facility savedFacility = facilityRepository.save(newFacility);
        facilityRepository.flush();

        return facilityResponseMapper.entityToResponseModel(savedFacility);
    }


    @Override
    public FacilityResponseModel updateFacility(FacilityRequestModel facilityRequestModel, String facilityId) {
        Facility existingFacility = facilityRepository.findByFacilityIdentifier_FacilityId(facilityId);
        if (existingFacility == null) {
            throw new NotFoundException("The provided ID [" + facilityId + "] does not match any facility.");
        }

        int minimumCapacity = 50;
        if (facilityRequestModel.getCapacity() != null
                && facilityRequestModel.getCapacity() < minimumCapacity) {
            throw new InsufficientFacilityCapacityException(
                    "The facility capacity is too low. A minimum capacity of " + minimumCapacity + " is required."
            );
        }


        FacilityIdentifier identifier = existingFacility.getFacilityIdentifier();
        Facility updatedFacility = facilityRequestMapper.requestModelToEntity(
                facilityRequestModel,
                identifier
        );
        updatedFacility.setId(existingFacility.getId());

        Facility savedFacility = facilityRepository.save(updatedFacility);
        return facilityResponseMapper.entityToResponseModel(savedFacility);
    }

    @Override
    public void deleteFacility(String facilityId) {
        Facility existingFacility = facilityRepository.findByFacilityIdentifier_FacilityId(facilityId);
        if (existingFacility == null) {
            throw new NotFoundException("Facility not found with ID: " + facilityId);
        }
        facilityRepository.delete(existingFacility);
    }
}
