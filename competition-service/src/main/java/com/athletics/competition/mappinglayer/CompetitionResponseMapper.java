package com.athletics.competition.mappinglayer;


import com.athletics.competition.dataaccesslayer.Competition;
import com.athletics.competition.presentationlayer.CompetitionController;
import com.athletics.competition.presentationlayer.CompetitionResponseModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.AfterMapping;

import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Mapper(componentModel = "spring")
public interface CompetitionResponseMapper {

    @Mappings({
            @Mapping(expression = "java(c.getCompetitionIdentifier().getCompetitionId())",   target = "competitionId"),
            @Mapping(source     = "competitionName",                                        target = "competitionName"),
            @Mapping(source     = "competitionDate",                                        target = "competitionDate"),
            @Mapping(source     = "competitionStatus",                                      target = "competitionStatus"),
            @Mapping(source     = "competitionResult",                                      target = "competitionResult"),

            @Mapping(expression = "java(c.getTeam().getTeamId())",                          target = "teamId"),
            @Mapping(expression = "java(c.getTeam().getTeamName())",                        target = "teamName"),
            @Mapping(expression = "java(c.getTeam().getCoachName())",                       target = "coachName"),
            @Mapping(expression = "java(c.getTeam().getTeamLevel())",                       target = "teamLevel"),

            @Mapping(expression = "java(c.getSponsor().getSponsorId())",                    target = "sponsorId"),
            @Mapping(expression = "java(c.getSponsor().getSponsorName())",                  target = "sponsorName"),
            @Mapping(expression = "java(c.getSponsor().getSponsorLevel())",                 target = "sponsorLevel"),
            @Mapping(expression = "java(c.getSponsor().getSponsorAmount())",                target = "sponsorAmount"),

            @Mapping(expression = "java(c.getFacility().getFacilityId())",                  target = "facilityId"),
            @Mapping(expression = "java(c.getFacility().getFacilityName())",                target = "facilityName"),
            @Mapping(expression = "java(c.getFacility().getCapacity())",                    target = "capacity"),
            @Mapping(expression = "java(c.getFacility().getLocation())",                    target = "location")
    })
    CompetitionResponseModel competitionEntityToCompetitionResponseModel(Competition c);

    List<CompetitionResponseModel> competitionEntityListToCompetitionResponseModelList(List<Competition> competitions);



}
