package com.athletics.team.mappinglayer.Athlete;

import com.athletics.team.dataaccesslayer.Athlete.Athlete;
import com.athletics.team.dataaccesslayer.Athlete.AthleteIdentifier;
import com.athletics.team.presentationlayer.Athlete.AthleteRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface AthleteRequestMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(expression = "java(athleteIdentifier)", target = "athleteIdentifier")
    })
    Athlete requestModelToEntity(AthleteRequestModel requestModel, AthleteIdentifier athleteIdentifier);
}
