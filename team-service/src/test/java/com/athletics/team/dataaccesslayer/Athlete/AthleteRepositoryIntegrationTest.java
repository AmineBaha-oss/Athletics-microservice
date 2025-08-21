package com.athletics.team.dataaccesslayer.Athlete;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AthleteRepositoryIntegrationTest {

    @Autowired
    private AthleteRepository athleteRepository;

    @BeforeEach
    public void setUp() {
        athleteRepository.deleteAll();
    }

    @Test
    public void whenAthleteEntityIsValid_thenAddAthlete() {
        Athlete athlete = new Athlete();
        athlete.setAthleteIdentifier(new AthleteIdentifier());
        athlete.setFirstName("Tom");
        athlete.setLastName("Hardy");
        athlete.setDateOfBirth(LocalDate.of(1980, 7, 15));
        athlete.setAthleteCategory(AthleteCategoryEnum.MASTER);
        athlete.setTeamId("TEAM100");

        Athlete saved = athleteRepository.save(athlete);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertNotNull(saved.getAthleteIdentifier().getAthleteId());
        assertEquals("Tom", saved.getFirstName());
        assertEquals("Hardy", saved.getLastName());
        assertEquals(AthleteCategoryEnum.MASTER, saved.getAthleteCategory());
        assertEquals("TEAM100", saved.getTeamId());
    }

    @Test
    public void whenFindAthletesByTeam_thenReturnAthletes() {
        Athlete a1 = new Athlete();
        a1.setAthleteIdentifier(new AthleteIdentifier());
        a1.setFirstName("Alice");
        a1.setLastName("Wonder");
        a1.setDateOfBirth(LocalDate.of(1992, 3, 12));
        a1.setAthleteCategory(AthleteCategoryEnum.JUNIOR);
        a1.setTeamId("TEAM200");

        Athlete a2 = new Athlete();
        a2.setAthleteIdentifier(new AthleteIdentifier());
        a2.setFirstName("Bob");
        a2.setLastName("Builder");
        a2.setDateOfBirth(LocalDate.of(1990, 6, 25));
        a2.setAthleteCategory(AthleteCategoryEnum.SENIOR);
        a2.setTeamId("TEAM200");

        athleteRepository.save(a1);
        athleteRepository.save(a2);

        List<Athlete> list = athleteRepository.findByTeamId("TEAM200");
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @Test
    public void whenFindAthleteByTeamAndAthleteId_thenReturnAthlete() {
        Athlete athlete = new Athlete();
        AthleteIdentifier iden = new AthleteIdentifier();
        athlete.setAthleteIdentifier(iden);
        athlete.setFirstName("Charlie");
        athlete.setLastName("Chaplin");
        athlete.setDateOfBirth(LocalDate.of(1985, 4, 16));
        athlete.setAthleteCategory(AthleteCategoryEnum.MASTER);
        athlete.setTeamId("TEAM300");

        athleteRepository.save(athlete);

        Athlete found = athleteRepository
                .findByTeamIdAndAthleteIdentifier_AthleteId("TEAM300", iden.getAthleteId());
        assertNotNull(found);
        assertEquals("Charlie", found.getFirstName());
        assertEquals("Chaplin", found.getLastName());
        assertEquals("TEAM300", found.getTeamId());
    }

    @Test
    public void whenFindAthleteByNonExistingCriteria_thenReturnNull() {
        Athlete found = athleteRepository
                .findByTeamIdAndAthleteIdentifier_AthleteId("TEAM999", "NON_EXISTENT");
        assertNull(found);
    }

    @Test
    public void whenCountAthletesByTeam_thenReturnCorrectCount() {
        Athlete a1 = new Athlete();
        a1.setAthleteIdentifier(new AthleteIdentifier());
        a1.setFirstName("Donna");
        a1.setLastName("Summer");
        a1.setDateOfBirth(LocalDate.of(1975, 12, 20));
        a1.setAthleteCategory(AthleteCategoryEnum.SENIOR);
        a1.setTeamId("TEAM400");

        Athlete a2 = new Athlete();
        a2.setAthleteIdentifier(new AthleteIdentifier());
        a2.setFirstName("Eve");
        a2.setLastName("Adam");
        a2.setDateOfBirth(LocalDate.of(1988, 11, 30));
        a2.setAthleteCategory(AthleteCategoryEnum.PARALYMPIC);
        a2.setTeamId("TEAM400");

        athleteRepository.save(a1);
        athleteRepository.save(a2);

        List<Athlete> list = athleteRepository.findByTeamId("TEAM400");
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @Test
    public void whenUpdatingAthlete_thenChangesArePersisted() {
        Athlete athlete = new Athlete();
        AthleteIdentifier iden = new AthleteIdentifier();
        athlete.setAthleteIdentifier(iden);
        athlete.setFirstName("Frank");
        athlete.setLastName("Sinatra");
        athlete.setDateOfBirth(LocalDate.of(1982, 8, 10));
        athlete.setAthleteCategory(AthleteCategoryEnum.SENIOR);
        athlete.setTeamId("TEAM500");

        Athlete saved = athleteRepository.save(athlete);
        saved.setFirstName("Franklin");
        saved.setLastName("Sinatra Updated");
        Athlete updated = athleteRepository.save(saved);

        assertNotNull(updated);
        assertEquals(iden.getAthleteId(), updated.getAthleteIdentifier().getAthleteId());
        assertEquals("Franklin", updated.getFirstName());
        assertEquals("Sinatra Updated", updated.getLastName());
    }

    @Test
    public void whenDeletingAthlete_thenAthleteIsRemoved() {
        Athlete athlete = new Athlete();
        AthleteIdentifier iden = new AthleteIdentifier();
        athlete.setAthleteIdentifier(iden);
        athlete.setFirstName("Grace");
        athlete.setLastName("Hopper");
        athlete.setDateOfBirth(LocalDate.of(1906, 12, 9));
        athlete.setAthleteCategory(AthleteCategoryEnum.MASTER);
        athlete.setTeamId("TEAM600");

        Athlete saved = athleteRepository.save(athlete);
        String athleteId = iden.getAthleteId();
        assertNotNull(
                athleteRepository.findByTeamIdAndAthleteIdentifier_AthleteId("TEAM600", athleteId)
        );

        athleteRepository.delete(saved);
        Athlete afterDelete = athleteRepository
                .findByTeamIdAndAthleteIdentifier_AthleteId("TEAM600", athleteId);
        assertNull(afterDelete);
    }
}







