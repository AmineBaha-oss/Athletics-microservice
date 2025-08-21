package com.athletics.team.mappinglayer.team;

import com.athletics.team.dataaccesslayer.Team.Team;
import com.athletics.team.presentationlayer.Team.TeamController;
import com.athletics.team.presentationlayer.Team.TeamResponseModel;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Mapper(componentModel = "spring")
public interface TeamResponseMapper {

    @Mappings({
            @Mapping(expression = "java(team.getTeamIdentifier().getTeamId())", target = "teamId"),
            @Mapping(source = "team.teamName", target = "teamName"),
            @Mapping(source = "team.coachName", target = "coachName"),
            @Mapping(source = "team.teamLevel", target = "teamLevel")
    })
    TeamResponseModel entityToResponseModel(Team team);

    List<TeamResponseModel> entityListToResponseModelList(List<Team> teams);


    }

