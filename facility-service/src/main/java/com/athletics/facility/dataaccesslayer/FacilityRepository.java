package com.athletics.facility.dataaccesslayer;

import org.springframework.data.jpa.repository.JpaRepository;


public interface FacilityRepository extends JpaRepository<Facility, Integer> {
    Facility findByFacilityIdentifier_FacilityId(String facilityId);
}
