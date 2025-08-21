package com.athletics.facility.dataaccesslayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FacilityRepositoryIntegrationTest {

    @Autowired
    private FacilityRepository facilityRepository;

    @BeforeEach
    void setupDb() {
        facilityRepository.deleteAll();
    }

    @Test
    void whenFacilitiesExist_thenReturnAllFacilities() {
        Facility f1 = new Facility(null, new FacilityIdentifier("fac1"), "Stadium1", 50000, "City1");
        Facility f2 = new Facility(null, new FacilityIdentifier("fac2"), "Arena2",   20000, "City2");
        facilityRepository.save(f1);
        facilityRepository.save(f2);
        long count = facilityRepository.count();

        List<Facility> list = facilityRepository.findAll();
        assertNotNull(list);
        assertEquals(count, list.size());
    }

    @Test
    void whenFacilityExists_thenReturnById() {
        Facility f = new Facility(null, new FacilityIdentifier("facX"), "Coliseum", 30000, "CityX");
        facilityRepository.save(f);

        Facility found = facilityRepository.findByFacilityIdentifier_FacilityId("facX");
        assertNotNull(found);
        assertEquals("facX", found.getFacilityIdentifier().getFacilityId());
        assertEquals("Coliseum", found.getFacilityName());
    }

    @Test
    void whenFacilityDoesNotExist_thenReturnNull() {
        Facility found = facilityRepository.findByFacilityIdentifier_FacilityId("no-fac");
        assertNull(found);
    }

    @Test
    void whenValidFacility_thenAddFacility() {
        Facility f = new Facility(null, new FacilityIdentifier("facY"), "FieldY", 15000, "CityY");
        Facility saved = facilityRepository.save(f);

        assertNotNull(saved.getId());
        assertNotNull(saved.getFacilityIdentifier().getFacilityId());
        assertEquals("FieldY", saved.getFacilityName());
        assertEquals(15000, saved.getCapacity());
        assertEquals("CityY", saved.getLocation());
    }

    @Test
    void whenUpdatingFacility_thenChangesPersisted() {
        Facility f = new Facility(null, new FacilityIdentifier("facZ"), "ParkZ", 10000, "CityZ");
        Facility saved = facilityRepository.save(f);

        saved.setFacilityName("ParkZZ");
        saved.setCapacity(12000);
        facilityRepository.save(saved);

        Facility found = facilityRepository.findByFacilityIdentifier_FacilityId("facZ");
        assertEquals("ParkZZ", found.getFacilityName());
        assertEquals(12000, found.getCapacity());
    }

    @Test
    void whenDeletingFacility_thenFacilityIsRemoved() {
        Facility f = new Facility(null, new FacilityIdentifier("facDel"), "GymDel", 8000, "CityDel");
        Facility saved = facilityRepository.save(f);
        String id = saved.getFacilityIdentifier().getFacilityId();

        assertNotNull(facilityRepository.findByFacilityIdentifier_FacilityId(id));
        facilityRepository.delete(saved);
        assertNull(facilityRepository.findByFacilityIdentifier_FacilityId(id));
    }


}
