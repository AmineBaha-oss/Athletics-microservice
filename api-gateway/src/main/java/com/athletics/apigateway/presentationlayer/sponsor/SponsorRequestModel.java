package com.athletics.apigateway.presentationlayer.sponsor;


import com.athletics.apigateway.domainclientlayer.sponsor.SponsorLevelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SponsorRequestModel {
    private String sponsorId;
    private String sponsorName;
    private SponsorLevelEnum sponsorLevel;
    private Double sponsorAmount;
}
