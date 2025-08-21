package com.athletics.facility.dataaccesslayer;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Embeddable
@Getter
@NoArgsConstructor
public class FacilityIdentifier {
    private String facilityId;

    public FacilityIdentifier(String facilityId) {
        this.facilityId = facilityId;
    }
}
