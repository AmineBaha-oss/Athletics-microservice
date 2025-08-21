package com.athletics.sponsor.businesslayer;



import com.athletics.sponsor.presentationlayer.SponsorRequestModel;
import com.athletics.sponsor.presentationlayer.SponsorResponseModel;
import com.athletics.sponsor.dataaccesslayer.SponsorLevelEnum;


import java.util.List;

public interface SponsorService {
    List<SponsorResponseModel> getAllSponsors();
    SponsorResponseModel getSponsorById(String sponsorId);
    SponsorResponseModel createSponsor(SponsorRequestModel sponsorRequestModel);
    SponsorResponseModel updateSponsor(SponsorRequestModel sponsorRequestModel, String sponsorId);

    SponsorResponseModel patchSponsorLevel(String sponsorId, SponsorLevelEnum newLevel);

    void deleteSponsor(String sponsorId);



}
