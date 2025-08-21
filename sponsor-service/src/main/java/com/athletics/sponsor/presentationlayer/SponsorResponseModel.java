package com.athletics.sponsor.presentationlayer;

import com.athletics.sponsor.dataaccesslayer.SponsorLevelEnum;
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
