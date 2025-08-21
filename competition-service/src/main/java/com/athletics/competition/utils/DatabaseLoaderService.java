package com.athletics.competition.utils;

import com.athletics.competition.dataaccesslayer.*;
import com.athletics.competition.domainclientLayer.facility.FacilityModel;
import com.athletics.competition.domainclientLayer.sponsor.SponsorModel;
import com.athletics.competition.domainclientLayer.team.TeamModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DatabaseLoaderService implements CommandLineRunner {

    @Autowired
    private CompetitionRepository competitionRepository;

    @Override
    public void run(String... args) {
        var team1 = TeamModel.builder()
                .teamId("11111111-1111-1111-1111-111111111111")
                .teamName("Montreal Eagles")
                .coachName("John Smith")
                .teamLevel("COLLEGE")
                .build();
        var sponsor1 = SponsorModel.builder()
                .sponsorId("aaaaaaa1-1aaa-1aaa-1aaa-aaaaaaaaaaa1")
                .sponsorName("Nike")
                .sponsorLevel("PLATINUM")
                .sponsorAmount(200_000.00)
                .build();
        var facility1 = FacilityModel.builder()
                .facilityId("fac11111-1111-1111-1111-111111111111")
                .facilityName("Olympic Stadium")
                .capacity(70_000)
                .location("Montreal, QC")
                .build();

        var comp1a = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("Spring Invitational A")
                .competitionDate(LocalDate.of(2025, 6, 1))
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.DRAW)
                .team(team1).sponsor(sponsor1).facility(facility1)
                .build();
        competitionRepository.save(comp1a);

        var comp1b = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("Spring Invitational B")
                .competitionDate(LocalDate.of(2025, 6, 15))
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.WIN)
                .team(team1).sponsor(sponsor1).facility(facility1)
                .build();
        competitionRepository.save(comp1b);

        var comp1c = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("Spring Invitational C")
                .competitionDate(LocalDate.of(2025, 7, 1))
                .competitionStatus(CompetitionStatusEnum.ONGOING)
                .competitionResult(CompetitionResultEnum.LOSS)
                .team(team1).sponsor(sponsor1).facility(facility1)
                .build();
        competitionRepository.save(comp1c);

        var comp1d = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("Spring Invitational D")
                .competitionDate(LocalDate.of(2025, 7, 15))
                .competitionStatus(CompetitionStatusEnum.COMPLETED)
                .competitionResult(CompetitionResultEnum.WIN)
                .team(team1).sponsor(sponsor1).facility(facility1)
                .build();
        competitionRepository.save(comp1d);

        var comp1e = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("Spring Invitational E")
                .competitionDate(LocalDate.of(2025, 8, 1))
                .competitionStatus(CompetitionStatusEnum.CANCELLED)
                .competitionResult(CompetitionResultEnum.DRAW)
                .team(team1).sponsor(sponsor1).facility(facility1)
                .build();
        competitionRepository.save(comp1e);

        var team2 = TeamModel.builder()
                .teamId("22222222-2222-2222-2222-222222222222")
                .teamName("Toronto Hawks")
                .coachName("Jane Doe")
                .teamLevel("PROFESSIONAL")
                .build();
        var sponsor2 = SponsorModel.builder()
                .sponsorId("aaaaaaa2-2aaa-2aaa-2aaa-aaaaaaaaaaa2")
                .sponsorName("Adidas")
                .sponsorLevel("GOLD")
                .sponsorAmount(150_000.00)
                .build();
        var facility2 = FacilityModel.builder()
                .facilityId("fac22222-2222-2222-2222-222222222222")
                .facilityName("Rogers Centre")
                .capacity(53_000)
                .location("Toronto, ON")
                .build();

        var comp2a = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("Autumn Championship A")
                .competitionDate(LocalDate.of(2025, 9, 1))
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.DRAW)
                .team(team2).sponsor(sponsor2).facility(facility2)
                .build();
        competitionRepository.save(comp2a);

        var comp2b = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("Autumn Championship B")
                .competitionDate(LocalDate.of(2025, 9, 15))
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.WIN)
                .team(team2).sponsor(sponsor2).facility(facility2)
                .build();
        competitionRepository.save(comp2b);

        var comp2c = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("Autumn Championship C")
                .competitionDate(LocalDate.of(2025, 10, 1))
                .competitionStatus(CompetitionStatusEnum.ONGOING)
                .competitionResult(CompetitionResultEnum.LOSS)
                .team(team2).sponsor(sponsor2).facility(facility2)
                .build();
        competitionRepository.save(comp2c);

        var comp2d = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("Autumn Championship D")
                .competitionDate(LocalDate.of(2025, 10, 15))
                .competitionStatus(CompetitionStatusEnum.COMPLETED)
                .competitionResult(CompetitionResultEnum.WIN)
                .team(team2).sponsor(sponsor2).facility(facility2)
                .build();
        competitionRepository.save(comp2d);

        var comp2e = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("Autumn Championship E")
                .competitionDate(LocalDate.of(2025, 11, 1))
                .competitionStatus(CompetitionStatusEnum.CANCELLED)
                .competitionResult(CompetitionResultEnum.DRAW)
                .team(team2).sponsor(sponsor2).facility(facility2)
                .build();
        competitionRepository.save(comp2e);

        var team3 = TeamModel.builder()
                .teamId("33333333-3333-3333-3333-333333333333")
                .teamName("Quebec Falcons")
                .coachName("Albert Martin")
                .teamLevel("NATIONAL")
                .build();
        var sponsor3 = SponsorModel.builder()
                .sponsorId("aaaaaaa3-3aaa-3aaa-3aaa-aaaaaaaaaaa3")
                .sponsorName("Puma")
                .sponsorLevel("SILVER")
                .sponsorAmount(100_000.00)
                .build();
        var facility3 = FacilityModel.builder()
                .facilityId("fac33333-3333-3333-3333-333333333333")
                .facilityName("BC Place")
                .capacity(54_000)
                .location("Vancouver, BC")
                .build();

        var comp3a = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("National Qualifier A")
                .competitionDate(LocalDate.of(2025, 7, 1))
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.DRAW)
                .team(team3).sponsor(sponsor3).facility(facility3)
                .build();
        competitionRepository.save(comp3a);

        var comp3b = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("National Qualifier B")
                .competitionDate(LocalDate.of(2025, 7, 20))
                .competitionStatus(CompetitionStatusEnum.ONGOING)
                .competitionResult(CompetitionResultEnum.LOSS)
                .team(team3).sponsor(sponsor3).facility(facility3)
                .build();
        competitionRepository.save(comp3b);

        var comp3c = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("National Qualifier C")
                .competitionDate(LocalDate.of(2025, 8, 5))
                .competitionStatus(CompetitionStatusEnum.COMPLETED)
                .competitionResult(CompetitionResultEnum.WIN)
                .team(team3).sponsor(sponsor3).facility(facility3)
                .build();
        competitionRepository.save(comp3c);

        var comp3d = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("National Qualifier D")
                .competitionDate(LocalDate.of(2025, 8, 20))
                .competitionStatus(CompetitionStatusEnum.CANCELLED)
                .competitionResult(CompetitionResultEnum.DRAW)
                .team(team3).sponsor(sponsor3).facility(facility3)
                .build();
        competitionRepository.save(comp3d);

        var comp3e = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("National Qualifier E")
                .competitionDate(LocalDate.of(2025, 9, 1))
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.WIN)
                .team(team3).sponsor(sponsor3).facility(facility3)
                .build();
        competitionRepository.save(comp3e);

    }
}
