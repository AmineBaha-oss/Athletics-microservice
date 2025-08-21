package com.athletics.apigateway.presentationlayer.facility;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacilityResponseModel extends RepresentationModel<FacilityResponseModel> {
    private String facilityId;
    private String facilityName;
    private Integer capacity;
    private String location;
}
