package com.athletics.competition.dataaccesslayer;

import com.athletics.competition.domainclientLayer.facility.FacilityModel;
import com.athletics.competition.domainclientLayer.sponsor.SponsorModel;
import com.athletics.competition.domainclientLayer.team.TeamModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class CompetitionRepositoryIntegrationTest {

    @Autowired
    private CompetitionRepository competitionRepository;

    private Competition comp1;
    private Competition comp2;
    private Competition compOtherTeam;

    private TeamModel team1;
    private TeamModel team2;
    private SponsorModel sponsor;
    private FacilityModel facility;

    @BeforeEach
    void setupDb() {
        competitionRepository.deleteAll();

        sponsor = SponsorModel.builder()
                .sponsorId("spons-1111-1111-1111-111111111111")
                .sponsorName("TestCorp")
                .sponsorLevel("GOLD")
                .sponsorAmount(50_000.0)
                .build();

        facility = FacilityModel.builder()
                .facilityId("fac-2222-2222-2222-222222222222")
                .facilityName("Test Arena")
                .capacity(10_000)
                .location("Testville")
                .build();

        team1 = TeamModel.builder()
                .teamId("team-1111-1111-1111-111111111111")
                .teamName("Team One")
                .coachName("Coach A")
                .teamLevel("COLLEGE")
                .build();

        team2 = TeamModel.builder()
                .teamId("team-2222-2222-2222-222222222222")
                .teamName("Team Two")
                .coachName("Coach B")
                .teamLevel("PROFESSIONAL")
                .build();

        comp1 = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("Alpha Cup")
                .competitionDate(LocalDate.of(2025, 5, 1))
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.WIN)
                .team(team1)
                .sponsor(sponsor)
                .facility(facility)
                .build();

        comp2 = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("Beta Bowl")
                .competitionDate(LocalDate.of(2025, 6, 15))
                .competitionStatus(CompetitionStatusEnum.ONGOING)
                .competitionResult(CompetitionResultEnum.LOSS)
                .team(team1)
                .sponsor(sponsor)
                .facility(facility)
                .build();

        compOtherTeam = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("Gamma Games")
                .competitionDate(LocalDate.of(2025, 7, 20))
                .competitionStatus(CompetitionStatusEnum.COMPLETED)
                .competitionResult(CompetitionResultEnum.DRAW)
                .team(team2)
                .sponsor(sponsor)
                .facility(facility)
                .build();

        competitionRepository.save(comp1);
        competitionRepository.save(comp2);
        competitionRepository.save(compOtherTeam);
    }

    @Test
    void whenFindAllByTeam_TeamId_thenReturnOnlyThatTeam() {
        List<Competition> list1 = competitionRepository.findAllByTeam_TeamId(team1.getTeamId());
        assertNotNull(list1);
        assertEquals(2, list1.size());
        list1.forEach(c -> assertEquals(team1.getTeamId(), c.getTeam().getTeamId()));

        List<Competition> list2 = competitionRepository.findAllByTeam_TeamId(team2.getTeamId());
        assertNotNull(list2);
        assertEquals(1, list2.size());
        assertEquals(team2.getTeamId(), list2.get(0).getTeam().getTeamId());
    }

    @Test
    void whenFindByTeamAndCompetitionId_exists_thenReturnEntity() {
        String id1 = comp1.getCompetitionIdentifier().getCompetitionId();
        Competition found = competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(
                        team1.getTeamId(), id1);

        assertNotNull(found);
        assertEquals(comp1.getCompetitionName(), found.getCompetitionName());
        assertEquals(id1, found.getCompetitionIdentifier().getCompetitionId());
    }

    @Test
    void whenFindByTeamAndCompetitionId_notFound_thenReturnNull() {
        Competition missing1 = competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(
                        team1.getTeamId(), "no-such-id");
        assertNull(missing1);

        Competition missing2 = competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(
                        "no-such-team", comp1.getCompetitionIdentifier().getCompetitionId());
        assertNull(missing2);
    }

    @Test
    void whenSaveNewCompetition_thenIdPopulatedAndCanBeFetched() {
        CompetitionIdentifier newIdent = new CompetitionIdentifier();
        Competition newComp = Competition.builder()
                .competitionIdentifier(newIdent)
                .competitionName("Delta Derby")
                .competitionDate(LocalDate.of(2025, 8, 30))
                .competitionStatus(CompetitionStatusEnum.CANCELLED)
                .competitionResult(CompetitionResultEnum.LOSS)
                .team(team1)
                .sponsor(sponsor)
                .facility(facility)
                .build();

        Competition saved = competitionRepository.save(newComp);
        assertNotNull(saved.getId(), "Mongo-generated _id should be set");
        Competition fetched = competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(
                        team1.getTeamId(), newIdent.getCompetitionId());
        assertNotNull(fetched);
        assertEquals("Delta Derby", fetched.getCompetitionName());
        assertEquals(CompetitionStatusEnum.CANCELLED, fetched.getCompetitionStatus());
    }

    @Test
    void whenUpdateCompetition_thenChangesArePersisted() {
        comp2.setCompetitionName("Beta Bowl Updated");
        comp2.setCompetitionStatus(CompetitionStatusEnum.COMPLETED);
        competitionRepository.save(comp2);

        Competition reloaded = competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(
                        team1.getTeamId(),
                        comp2.getCompetitionIdentifier().getCompetitionId()
                );
        assertNotNull(reloaded);
        assertEquals("Beta Bowl Updated", reloaded.getCompetitionName());
        assertEquals(CompetitionStatusEnum.COMPLETED, reloaded.getCompetitionStatus());
    }

    @Test
    void whenDeleteCompetition_thenItIsRemoved() {
        competitionRepository.delete(comp1);

        Competition shouldBeNull = competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(
                        team1.getTeamId(),
                        comp1.getCompetitionIdentifier().getCompetitionId()
                );
        assertNull(shouldBeNull);
        List<Competition> remaining = competitionRepository.findAllByTeam_TeamId(team1.getTeamId());
        assertEquals(1, remaining.size());
    }
}
