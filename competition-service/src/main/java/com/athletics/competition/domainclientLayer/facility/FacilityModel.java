package com.athletics.competition.domainclientLayer.facility;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class FacilityModel {
    private String facilityId;
    private String facilityName;
    private Integer capacity;
    private String location;
}
