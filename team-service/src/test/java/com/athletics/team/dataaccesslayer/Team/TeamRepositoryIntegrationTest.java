package com.athletics.team.dataaccesslayer.Team;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TeamRepositoryIntegrationTest {

    @Autowired
    private TeamRepository teamRepository;

    @BeforeEach
    void setupDb() {
        teamRepository.deleteAll();
    }

    @Test
    void whenTeamsExist_thenReturnAllTeams() {
        Team t1 = new Team(null, new TeamIdentifier("team1"), "Eagles", "Smith", TeamLevelEnum.COLLEGE);
        Team t2 = new Team(null, new TeamIdentifier("team2"), "Hawks", "Doe", TeamLevelEnum.PROFESSIONAL);
        teamRepository.save(t1);
        teamRepository.save(t2);
        long count = teamRepository.count();

        List<Team> list = teamRepository.findAll();
        assertNotNull(list);
        assertEquals(count, list.size());
    }

    @Test
    void whenTeamExists_thenReturnTeamById() {
        Team t = new Team(null, new TeamIdentifier("teamX"), "Falcons", "Brown", TeamLevelEnum.NATIONAL);
        teamRepository.save(t);

        Team found = teamRepository.findByTeamIdentifier_TeamId("teamX");
        assertNotNull(found);
        assertEquals("teamX", found.getTeamIdentifier().getTeamId());
        assertEquals("Falcons", found.getTeamName());
    }

    @Test
    void whenTeamDoesNotExist_thenReturnNull() {
        Team found = teamRepository.findByTeamIdentifier_TeamId("no-such-team");
        assertNull(found);
    }

    @Test
    void whenTeamEntityIsValid_thenAddTeam() {
        Team t = new Team(null, new TeamIdentifier("teamY"), "Wolves", "Li", TeamLevelEnum.HIGH_SCHOOL);
        Team saved = teamRepository.save(t);

        assertNotNull(saved.getId());
        assertNotNull(saved.getTeamIdentifier());
        assertEquals("teamY", saved.getTeamIdentifier().getTeamId());
        assertEquals("Wolves", saved.getTeamName());
        assertEquals("Li", saved.getCoachName());
        assertEquals(TeamLevelEnum.HIGH_SCHOOL, saved.getTeamLevel());
    }

    @Test
    void whenUpdatingTeam_thenChangesArePersisted() {
        Team t = new Team(null, new TeamIdentifier("teamZ"), "Bears", "Spencer", TeamLevelEnum.COLLEGE);
        Team saved = teamRepository.save(t);

        saved.setTeamName("Bears Updated");
        saved.setCoachName("Spencer Jr.");
        saved.setTeamLevel(TeamLevelEnum.PROFESSIONAL);
        teamRepository.save(saved);

        Team found = teamRepository.findByTeamIdentifier_TeamId("teamZ");
        assertEquals("Bears Updated", found.getTeamName());
        assertEquals("Spencer Jr.", found.getCoachName());
        assertEquals(TeamLevelEnum.PROFESSIONAL, found.getTeamLevel());
    }

    @Test
    void whenDeletingTeam_thenTeamIsRemoved() {
        Team t = new Team(null, new TeamIdentifier("teamDel"), "Wolves", "Clark", TeamLevelEnum.NATIONAL);
        Team saved = teamRepository.save(t);
        String id = saved.getTeamIdentifier().getTeamId();

        assertNotNull(teamRepository.findByTeamIdentifier_TeamId(id));
        teamRepository.delete(saved);
        assertNull(teamRepository.findByTeamIdentifier_TeamId(id));
    }
}
