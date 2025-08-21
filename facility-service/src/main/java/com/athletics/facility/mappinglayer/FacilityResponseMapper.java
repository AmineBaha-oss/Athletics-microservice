package com.athletics.facility.mappinglayer;

import com.athletics.facility.dataaccesslayer.Facility;
import com.athletics.facility.presentationlayer.FacilityController;
import com.athletics.facility.presentationlayer.FacilityResponseModel;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Mapper(componentModel = "spring")
public interface FacilityResponseMapper {

    @Mappings({
            @Mapping(expression = "java(facility.getFacilityIdentifier().getFacilityId())", target = "facilityId"),
            @Mapping(source = "facility.facilityName", target = "facilityName"),
            @Mapping(source = "facility.capacity", target = "capacity"),
            @Mapping(source = "facility.location", target = "location")
    })
    FacilityResponseModel entityToResponseModel(Facility facility);

    List<FacilityResponseModel> entityListToResponseModelList(List<Facility> facilities);


}
