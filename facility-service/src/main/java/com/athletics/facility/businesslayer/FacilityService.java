package com.athletics.facility.businesslayer;


import com.athletics.facility.presentationlayer.FacilityRequestModel;
import com.athletics.facility.presentationlayer.FacilityResponseModel;

import java.util.List;


public interface FacilityService {
    List<FacilityResponseModel> getAllFacilities();
    FacilityResponseModel getFacilityById(String facilityId);
    FacilityResponseModel createFacility(FacilityRequestModel facilityRequestModel);
    FacilityResponseModel updateFacility(FacilityRequestModel facilityRequestModel, String facilityId);
    void deleteFacility(String facilityId);
}
