package com.athletics.sponsor.mappinglayer;

import com.athletics.sponsor.dataaccesslayer.Sponsor;
import com.athletics.sponsor.presentationlayer.SponsorController;
import com.athletics.sponsor.presentationlayer.SponsorResponseModel;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Mapper(componentModel = "spring")
public interface SponsorResponseMapper {

    @Mappings({
            @Mapping(expression = "java(sponsor.getSponsorIdentifier().getSponsorId())", target = "sponsorId"),
            @Mapping(source = "sponsor.sponsorName", target = "sponsorName"),
            @Mapping(source = "sponsor.sponsorLevel", target = "sponsorLevel"),
            @Mapping(source = "sponsor.sponsorAmount", target = "sponsorAmount")
    })
    SponsorResponseModel entityToResponseModel(Sponsor sponsor);

    List<SponsorResponseModel> entityListToResponseModelList(List<Sponsor> sponsors);


}
