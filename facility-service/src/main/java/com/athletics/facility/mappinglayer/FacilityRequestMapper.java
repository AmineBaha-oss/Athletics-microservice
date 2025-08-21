package com.athletics.facility.mappinglayer;

import com.athletics.facility.dataaccesslayer.Facility;
import com.athletics.facility.dataaccesslayer.FacilityIdentifier;
import com.athletics.facility.presentationlayer.FacilityRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface FacilityRequestMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(expression = "java(facilityIdentifier)", target = "facilityIdentifier")
    })
    Facility requestModelToEntity(FacilityRequestModel facilityRequestModel,
                                  FacilityIdentifier facilityIdentifier);
}
