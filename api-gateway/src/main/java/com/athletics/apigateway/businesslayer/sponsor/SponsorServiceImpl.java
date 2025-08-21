package com.athletics.apigateway.businesslayer.sponsor;

import com.athletics.apigateway.domainclientlayer.sponsor.SponsorServiceClient;
import com.athletics.apigateway.presentationlayer.sponsor.SponsorRequestModel;
import com.athletics.apigateway.presentationlayer.sponsor.SponsorResponseModel;
import com.athletics.apigateway.presentationlayer.sponsor.SponsorController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@Service
public class SponsorServiceImpl implements SponsorService {

    private final SponsorServiceClient sponsorServiceClient;

    public SponsorServiceImpl(SponsorServiceClient sponsorServiceClient) {
        this.sponsorServiceClient = sponsorServiceClient;
    }

    @Override
    public List<SponsorResponseModel> getAllSponsors() {
        log.debug("Business Layer: getAllSponsors() called");
        List<SponsorResponseModel> sponsors = sponsorServiceClient.getAllSponsors();
        for (SponsorResponseModel sponsor : sponsors) {
            addHateoasLinks(sponsor);
        }
        return sponsors;
    }

    @Override
    public SponsorResponseModel getSponsorById(String sponsorId) {
        log.debug("Business Layer: getSponsorById({}) called", sponsorId);
        SponsorResponseModel sponsor = sponsorServiceClient.getSponsorById(sponsorId);
        return addHateoasLinks(sponsor);
    }

    @Override
    public SponsorResponseModel createSponsor(SponsorRequestModel sponsorRequestModel) {
        log.debug("Business Layer: createSponsor() called");
        SponsorResponseModel createdSponsor = sponsorServiceClient.createSponsor(sponsorRequestModel);
        return addHateoasLinks(createdSponsor);
    }

    @Override
    public SponsorResponseModel updateSponsor(String sponsorId, SponsorRequestModel sponsorRequestModel) {
        log.debug("Business Layer: updateSponsor({}) called", sponsorId);
        SponsorResponseModel updatedSponsor = sponsorServiceClient.updateSponsor(sponsorId, sponsorRequestModel);
        return addHateoasLinks(updatedSponsor);
    }

    @Override
    public void deleteSponsor(String sponsorId) {
        log.debug("Business Layer: deleteSponsor({}) called", sponsorId);
        sponsorServiceClient.deleteSponsor(sponsorId);
    }

    private SponsorResponseModel addHateoasLinks(SponsorResponseModel sponsor) {
        Link selfLink = linkTo(methodOn(SponsorController.class)
                .getSponsorById(sponsor.getSponsorId()))
                .withSelfRel();
        sponsor.add(selfLink);

        Link allSponsorsLink = linkTo(methodOn(SponsorController.class)
                .getAllSponsors())
                .withRel("allSponsors");
        sponsor.add(allSponsorsLink);

        return sponsor;
    }
}
