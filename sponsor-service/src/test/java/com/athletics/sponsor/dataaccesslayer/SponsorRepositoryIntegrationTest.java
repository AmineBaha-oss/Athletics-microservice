package com.athletics.sponsor.dataaccesslayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class SponsorRepositoryIntegrationTest {

    @Autowired
    private SponsorRepository sponsorRepository;

    @BeforeEach
    void setupDb() {
        sponsorRepository.deleteAll();
    }

    @Test
    void whenSponsorsExist_thenReturnAllSponsors() {
        Sponsor s1 = new Sponsor(null, new SponsorIdentifier("s1"), "Alpha", SponsorLevelEnum.GOLD,    5000.0);
        Sponsor s2 = new Sponsor(null, new SponsorIdentifier("s2"), "Beta",  SponsorLevelEnum.SILVER,  3000.0);
        sponsorRepository.save(s1);
        sponsorRepository.save(s2);
        long count = sponsorRepository.count();

        List<Sponsor> list = sponsorRepository.findAll();
        assertNotNull(list);
        assertEquals(count, list.size());
    }

    @Test
    void whenSponsorExists_thenReturnById() {
        Sponsor s = new Sponsor(null, new SponsorIdentifier("sX"), "Gamma", SponsorLevelEnum.BRONZE, 1500.0);
        sponsorRepository.save(s);

        Sponsor found = sponsorRepository.findBySponsorIdentifier_SponsorId("sX");
        assertNotNull(found);
        assertEquals("sX", found.getSponsorIdentifier().getSponsorId());
        assertEquals("Gamma", found.getSponsorName());
    }

    @Test
    void whenSponsorDoesNotExist_thenReturnNull() {
        Sponsor found = sponsorRepository.findBySponsorIdentifier_SponsorId("no-sp");
        assertNull(found);
    }

    @Test
    void whenValidSponsor_thenAddSponsor() {
        Sponsor s = new Sponsor(null, new SponsorIdentifier("sY"), "Delta", SponsorLevelEnum.PLATINUM, 8000.0);
        Sponsor saved = sponsorRepository.save(s);

        assertNotNull(saved.getId());
        assertNotNull(saved.getSponsorIdentifier().getSponsorId());
        assertEquals("Delta", saved.getSponsorName());
        assertEquals(SponsorLevelEnum.PLATINUM, saved.getSponsorLevel());
        assertEquals(8000.0, saved.getSponsorAmount());
    }

    @Test
    void whenUpdatingSponsor_thenChangesPersisted() {
        Sponsor s = new Sponsor(null, new SponsorIdentifier("sZ"), "Epsilon", SponsorLevelEnum.GOLD, 6000.0);
        Sponsor saved = sponsorRepository.save(s);

        saved.setSponsorName("EpsilonUpdated");
        saved.setSponsorAmount(7000.0);
        sponsorRepository.save(saved);

        Sponsor found = sponsorRepository.findBySponsorIdentifier_SponsorId("sZ");
        assertEquals("EpsilonUpdated", found.getSponsorName());
        assertEquals(7000.0, found.getSponsorAmount());
    }

    @Test
    void whenDeletingSponsor_thenSponsorIsRemoved() {
        Sponsor s = new Sponsor(null, new SponsorIdentifier("sDel"), "Zeta", SponsorLevelEnum.SILVER, 4000.0);
        Sponsor saved = sponsorRepository.save(s);
        String id = saved.getSponsorIdentifier().getSponsorId();

        assertNotNull(sponsorRepository.findBySponsorIdentifier_SponsorId(id));
        sponsorRepository.delete(saved);
        assertNull(sponsorRepository.findBySponsorIdentifier_SponsorId(id));
    }

}
