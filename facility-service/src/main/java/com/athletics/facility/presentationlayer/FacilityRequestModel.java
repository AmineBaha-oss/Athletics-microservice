package com.athletics.facility.presentationlayer;

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
