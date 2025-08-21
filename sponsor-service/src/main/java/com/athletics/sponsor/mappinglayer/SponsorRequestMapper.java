package com.athletics.sponsor.mappinglayer;

import com.athletics.sponsor.dataaccesslayer.Sponsor;
import com.athletics.sponsor.dataaccesslayer.SponsorIdentifier;
import com.athletics.sponsor.presentationlayer.SponsorRequestModel;


@Mapper(componentModel = "spring")
public interface SponsorRequestMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(expression = "java(sponsorIdentifier)", target = "sponsorIdentifier")
    })
    Sponsor requestModelToEntity(SponsorRequestModel sponsorRequestModel,
                                 SponsorIdentifier sponsorIdentifier);
}
