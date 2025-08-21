package com.athletics.apigateway.presentationlayer.facility;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacilityRequestModel {
    private String facilityId;
    private String facilityName;
    private Integer capacity;
    private String location;
}
