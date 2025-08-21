package com.athletics.apigateway.presentationlayer.sponsor;


import com.athletics.apigateway.domainclientlayer.sponsor.SponsorLevelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SponsorResponseModel extends RepresentationModel<SponsorResponseModel> {
    private String sponsorId;
    private String sponsorName;
    private SponsorLevelEnum sponsorLevel;
    private Double sponsorAmount;
}
