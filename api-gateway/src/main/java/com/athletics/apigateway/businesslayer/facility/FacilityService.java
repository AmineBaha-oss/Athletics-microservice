package com.athletics.apigateway.businesslayer.facility;



import com.athletics.apigateway.presentationlayer.facility.FacilityRequestModel;
import com.athletics.apigateway.presentationlayer.facility.FacilityResponseModel;

import java.util.List;

public interface FacilityService {
    List<FacilityResponseModel> getAllFacilities();
    FacilityResponseModel getFacilityById(String facilityId);
    FacilityResponseModel createFacility(FacilityRequestModel facilityRequestModel);
    FacilityResponseModel updateFacility(String facilityId, FacilityRequestModel facilityRequestModel);
    void deleteFacility(String facilityId);
}
