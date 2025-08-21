package com.athletics.team.mappinglayer.Athlete;

import com.athletics.team.dataaccesslayer.Athlete.Athlete;
import com.athletics.team.presentationlayer.Athlete.AthleteController;
import com.athletics.team.presentationlayer.Athlete.AthleteResponseModel;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Mapper(componentModel = "spring")
public interface AthleteResponseMapper {

    @Mappings({
            @Mapping(expression = "java(athlete.getAthleteIdentifier().getAthleteId())", target = "athleteId"),
            @Mapping(source = "athlete.firstName", target = "firstName"),
            @Mapping(source = "athlete.lastName", target = "lastName"),
            @Mapping(source = "athlete.dateOfBirth", target = "dateOfBirth"),
            @Mapping(source = "athlete.athleteCategory", target = "athleteCategory")
    })
    AthleteResponseModel entityToResponseModel(Athlete athlete);

    List<AthleteResponseModel> entityListToResponseModelList(List<Athlete> athletes);



}
