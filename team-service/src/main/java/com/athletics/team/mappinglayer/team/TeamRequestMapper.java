package com.athletics.team.mappinglayer.team;

import com.athletics.team.dataaccesslayer.Team.Team;
import com.athletics.team.dataaccesslayer.Team.TeamIdentifier;
import com.athletics.team.presentationlayer.Team.TeamRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface TeamRequestMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(expression = "java(teamIdentifier)", target = "teamIdentifier")
    })
    Team requestModelToEntity(TeamRequestModel teamRequestModel,
                              TeamIdentifier teamIdentifier);
}
