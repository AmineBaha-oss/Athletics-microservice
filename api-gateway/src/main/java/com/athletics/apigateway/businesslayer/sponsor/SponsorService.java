package com.athletics.apigateway.businesslayer.sponsor;



import com.athletics.apigateway.presentationlayer.sponsor.SponsorRequestModel;
import com.athletics.apigateway.presentationlayer.sponsor.SponsorResponseModel;

import java.util.List;

public interface SponsorService {
    List<SponsorResponseModel> getAllSponsors();
    SponsorResponseModel getSponsorById(String sponsorId);
    SponsorResponseModel createSponsor(SponsorRequestModel sponsorRequestModel);
    SponsorResponseModel updateSponsor(String sponsorId, SponsorRequestModel sponsorRequestModel);
    void deleteSponsor(String sponsorId);
}
