package com.athletics.competition.mappinglayer;

import com.athletics.competition.dataaccesslayer.Competition;
import com.athletics.competition.dataaccesslayer.CompetitionIdentifier;
import com.athletics.competition.domainclientLayer.facility.FacilityModel;
import com.athletics.competition.domainclientLayer.sponsor.SponsorModel;
import com.athletics.competition.domainclientLayer.team.TeamModel;
import com.athletics.competition.presentationlayer.CompetitionRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CompetitionRequestMapper {

    @Mappings({
            @Mapping(source = "competitionRequestModel.competitionName",   target = "competitionName"),
            @Mapping(source = "competitionRequestModel.competitionDate",   target = "competitionDate"),
            @Mapping(source = "competitionRequestModel.competitionStatus", target = "competitionStatus"),
            @Mapping(source = "competitionRequestModel.competitionResult", target = "competitionResult")
    })
    Competition requestModelToEntity(
            CompetitionRequestModel   competitionRequestModel,
            CompetitionIdentifier     competitionIdentifier,
            TeamModel                 team,
            SponsorModel              sponsor,
            FacilityModel             facility
    );
}
