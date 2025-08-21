package com.athletics.competition.businesslayer;

import com.athletics.competition.dataaccesslayer.Competition;
import com.athletics.competition.dataaccesslayer.CompetitionIdentifier;
import com.athletics.competition.dataaccesslayer.CompetitionRepository;
import com.athletics.competition.dataaccesslayer.CompetitionResultEnum;
import com.athletics.competition.dataaccesslayer.CompetitionStatusEnum;
import com.athletics.competition.domainclientLayer.facility.FacilityModel;
import com.athletics.competition.domainclientLayer.facility.FacilityServiceClient;
import com.athletics.competition.domainclientLayer.sponsor.SponsorModel;
import com.athletics.competition.domainclientLayer.sponsor.SponsorServiceClient;
import com.athletics.competition.domainclientLayer.team.TeamModel;
import com.athletics.competition.domainclientLayer.team.TeamServiceClient;
import com.athletics.competition.mappinglayer.CompetitionRequestMapper;
import com.athletics.competition.mappinglayer.CompetitionResponseMapper;
import com.athletics.competition.presentationlayer.CompetitionRequestModel;
import com.athletics.competition.presentationlayer.CompetitionResponseModel;
import com.athletics.competition.utils.exceptions.CompetitionDateTooFarException;
import com.athletics.competition.utils.exceptions.InvalidInputException;
import com.athletics.competition.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration")
@ActiveProfiles("test")
class CompetitionServiceUnitTest {

    @Autowired
    private CompetitionService competitionService;

    @MockitoBean
    private TeamServiceClient teamServiceClient;

    @MockitoBean
    private SponsorServiceClient sponsorServiceClient;

    @MockitoBean
    private FacilityServiceClient facilityServiceClient;

    @MockitoBean
    private CompetitionRepository competitionRepository;

    @MockitoSpyBean
    private CompetitionRequestMapper competitionRequestMapper;

    @MockitoSpyBean
    private CompetitionResponseMapper competitionResponseMapper;

    private final String TEAM_ID     = "11111111-1111-1111-1111-111111111111";
    private final String SPONSOR_ID  = "aaaaaaa1-1aaa-1aaa-1aaa-aaaaaaaaaaa1";
    private final String FACILITY_ID = "fac11111-1111-1111-1111-111111111111";

    private TeamModel team;
    private SponsorModel sponsor;
    private FacilityModel facility;
    private CompetitionRequestModel validRequest;

    @BeforeEach
    void setUp() {
        team = TeamModel.builder()
                .teamId(TEAM_ID)
                .teamName("Montreal Eagles")
                .coachName("John Smith")
                .teamLevel("COLLEGE")
                .build();

        sponsor = SponsorModel.builder()
                .sponsorId(SPONSOR_ID)
                .sponsorName("Nike")
                .sponsorLevel("PLATINUM")
                .sponsorAmount(200_000.0)
                .build();

        facility = FacilityModel.builder()
                .facilityId(FACILITY_ID)
                .facilityName("Olympic Stadium")
                .capacity(70_000)
                .location("Montreal, QC")
                .build();

        validRequest = CompetitionRequestModel.builder()
                .competitionName("Test Championship")
                .competitionDate(LocalDate.now().plusDays(1))
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.DRAW)
                .sponsorId(SPONSOR_ID)
                .facilityId(FACILITY_ID)
                .build();
    }


    @Test
    void whenGetAllCompetitions_thenReturnMappedList() {
        Competition compA = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("A")
                .competitionDate(LocalDate.now())
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.WIN)
                .team(team)
                .sponsor(sponsor)
                .facility(facility)
                .build();
        Competition compB = Competition.builder()
                .competitionIdentifier(new CompetitionIdentifier())
                .competitionName("B")
                .competitionDate(LocalDate.now())
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.LOSS)
                .team(team)
                .sponsor(sponsor)
                .facility(facility)
                .build();

        when(teamServiceClient.getTeamByTeamId(TEAM_ID)).thenReturn(team);
        when(competitionRepository.findAllByTeam_TeamId(TEAM_ID))
                .thenReturn(Arrays.asList(compA, compB));

        when(competitionResponseMapper.competitionEntityToCompetitionResponseModel(any()))
                .thenAnswer(inv -> {
                    Competition c = inv.getArgument(0);
                    CompetitionResponseModel dto = new CompetitionResponseModel();
                    dto.setCompetitionId(c.getCompetitionIdentifier().getCompetitionId());
                    return dto;
                });

        List<CompetitionResponseModel> all = competitionService.getAllCompetitions(TEAM_ID);

        assertEquals(2, all.size());
        assertEquals(compA.getCompetitionIdentifier().getCompetitionId(), all.get(0).getCompetitionId());
        verify(teamServiceClient, times(1)).getTeamByTeamId(TEAM_ID);
        verify(competitionResponseMapper, times(2))
                .competitionEntityToCompetitionResponseModel(any());
    }

    @Test
    void whenGetAllCompetitions_teamNotFound_thenThrow() {
        when(teamServiceClient.getTeamByTeamId(TEAM_ID))
                .thenThrow(new NotFoundException("no such team"));
        assertThrows(NotFoundException.class,
                () -> competitionService.getAllCompetitions(TEAM_ID));
    }


    @Test
    void whenGetCompetitionByIdExists_thenReturnMapped() {
        CompetitionIdentifier cid = new CompetitionIdentifier();
        Competition comp = Competition.builder()
                .competitionIdentifier(cid)
                .competitionName("X")
                .competitionDate(LocalDate.now())
                .competitionStatus(CompetitionStatusEnum.ONGOING)
                .competitionResult(CompetitionResultEnum.DRAW)
                .team(team)
                .sponsor(sponsor)
                .facility(facility)
                .build();

        when(teamServiceClient.getTeamByTeamId(TEAM_ID)).thenReturn(team);
        when(competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(TEAM_ID, cid.getCompetitionId()))
                .thenReturn(comp);

        when(competitionResponseMapper.competitionEntityToCompetitionResponseModel(comp))
                .thenAnswer(inv -> {
                    Competition c = inv.getArgument(0);
                    CompetitionResponseModel dto = new CompetitionResponseModel();
                    dto.setCompetitionId(c.getCompetitionIdentifier().getCompetitionId());
                    return dto;
                });

        CompetitionResponseModel result =
                competitionService.getCompetitionById(TEAM_ID, cid.getCompetitionId());

        assertEquals(cid.getCompetitionId(), result.getCompetitionId());
        verify(teamServiceClient).getTeamByTeamId(TEAM_ID);
        verify(competitionResponseMapper).competitionEntityToCompetitionResponseModel(comp);
    }

    @Test
    void whenGetCompetitionByIdNotFound_thenThrow() {
        when(teamServiceClient.getTeamByTeamId(TEAM_ID)).thenReturn(team);
        when(competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(TEAM_ID, "no-id"))
                .thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> competitionService.getCompetitionById(TEAM_ID, "no-id"));
    }


    @Test
    void whenCreateCompetition_happyPath_thenReturnsDtoWithSameRandomId() {
        when(teamServiceClient.getTeamByTeamId(TEAM_ID)).thenReturn(team);
        when(sponsorServiceClient.getSponsorBySponsorId(SPONSOR_ID)).thenReturn(sponsor);
        when(facilityServiceClient.getFacilityByFacilityId(FACILITY_ID)).thenReturn(facility);

        doAnswer(inv -> {
            CompetitionRequestModel r = inv.getArgument(0);
            CompetitionIdentifier newId       = inv.getArgument(1);
            TeamModel       t                  = inv.getArgument(2);
            SponsorModel    s                  = inv.getArgument(3);
            FacilityModel   f                  = inv.getArgument(4);
            return Competition.builder()
                    .competitionIdentifier(newId)
                    .competitionName(r.getCompetitionName())
                    .competitionDate(r.getCompetitionDate())
                    .competitionStatus(r.getCompetitionStatus())
                    .competitionResult(r.getCompetitionResult())
                    .team(t)
                    .sponsor(s)
                    .facility(f)
                    .build();
        }).when(competitionRequestMapper)
                .requestModelToEntity(
                        eq(validRequest),
                        any(CompetitionIdentifier.class),
                        eq(team),
                        eq(sponsor),
                        eq(facility)
                );

        ArgumentCaptor<Competition> savedCaptor = ArgumentCaptor.forClass(Competition.class);
        when(competitionRepository.save(savedCaptor.capture()))
                .thenAnswer(inv -> inv.getArgument(0));

        CompetitionResponseModel dto = competitionService.createCompetition(TEAM_ID, validRequest);

        assertNotNull(dto);
        String generated = savedCaptor.getValue()
                .getCompetitionIdentifier()
                .getCompetitionId();
        assertEquals(generated, dto.getCompetitionId());

        verify(competitionRequestMapper).requestModelToEntity(
                eq(validRequest), any(CompetitionIdentifier.class), eq(team), eq(sponsor), eq(facility)
        );
        verify(competitionRepository).save(any());
    }

    @Test
    void whenCreateCompetition_dateTooFar_thenThrow() {
        CompetitionRequestModel far = CompetitionRequestModel.builder()
                .competitionName("TooFar")
                .competitionDate(LocalDate.now().plusYears(2))
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.DRAW)
                .sponsorId(SPONSOR_ID)
                .facilityId(FACILITY_ID)
                .build();

        assertThrows(CompetitionDateTooFarException.class,
                () -> competitionService.createCompetition(TEAM_ID, far));
    }

    @Test
    void whenCreateCompetition_unknownTeam_thenThrow() {
        when(teamServiceClient.getTeamByTeamId(TEAM_ID)).thenReturn(null);
        assertThrows(InvalidInputException.class,
                () -> competitionService.createCompetition(TEAM_ID, validRequest));
    }

    @Test
    void whenCreateCompetition_unknownSponsor_thenThrow() {
        when(teamServiceClient.getTeamByTeamId(TEAM_ID)).thenReturn(team);
        when(sponsorServiceClient.getSponsorBySponsorId(SPONSOR_ID)).thenReturn(null);

        assertThrows(InvalidInputException.class,
                () -> competitionService.createCompetition(TEAM_ID, validRequest));
    }

    @Test
    void whenCreateCompetition_unknownFacility_thenThrow() {
        when(teamServiceClient.getTeamByTeamId(TEAM_ID)).thenReturn(team);
        when(sponsorServiceClient.getSponsorBySponsorId(SPONSOR_ID)).thenReturn(sponsor);
        when(facilityServiceClient.getFacilityByFacilityId(FACILITY_ID)).thenReturn(null);

        assertThrows(InvalidInputException.class,
                () -> competitionService.createCompetition(TEAM_ID, validRequest));
    }

    @Test
    void whenCreateCompetition_statusCompleted_triggersSponsorPatch() {
        validRequest.setCompetitionStatus(CompetitionStatusEnum.COMPLETED);

        when(teamServiceClient.getTeamByTeamId(TEAM_ID)).thenReturn(team);
        when(sponsorServiceClient.getSponsorBySponsorId(SPONSOR_ID)).thenReturn(sponsor);
        when(facilityServiceClient.getFacilityByFacilityId(FACILITY_ID)).thenReturn(facility);

        doAnswer(inv -> Competition.builder()
                .competitionIdentifier(inv.getArgument(1))
                .competitionName(validRequest.getCompetitionName())
                .competitionDate(validRequest.getCompetitionDate())
                .competitionStatus(CompetitionStatusEnum.COMPLETED)
                .competitionResult(validRequest.getCompetitionResult())
                .team(team)
                .sponsor(sponsor)
                .facility(facility)
                .build()
        ).when(competitionRequestMapper)
                .requestModelToEntity(
                        eq(validRequest),
                        any(CompetitionIdentifier.class),
                        eq(team),
                        eq(sponsor),
                        eq(facility)
                );

        when(competitionRepository.save(any(Competition.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(sponsorServiceClient.patchSponsorLevelBySponsorId(
                eq(SPONSOR_ID), anyString()))
                .thenReturn(
                        SponsorModel.builder()
                                .sponsorId(SPONSOR_ID)
                                .sponsorName("Nike")
                                .sponsorLevel("PLATINUM")
                                .sponsorAmount(0.0)
                                .build()
                );

        CompetitionResponseModel dto = competitionService.createCompetition(TEAM_ID, validRequest);

        assertNotNull(dto);
        verify(sponsorServiceClient, times(1))
                .patchSponsorLevelBySponsorId(eq(SPONSOR_ID), anyString());
        verify(competitionRepository, times(2)).save(any(Competition.class));
    }


    @Test
    void whenUpdateCompetition_happyPath_thenMapAndReturn() {
        CompetitionIdentifier existingId = new CompetitionIdentifier();
        Competition existing = Competition.builder()
                .competitionIdentifier(existingId)
                .competitionName("Old")
                .competitionDate(LocalDate.now())
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.DRAW)
                .team(team)
                .sponsor(sponsor)
                .facility(facility)
                .build();

        when(teamServiceClient.getTeamByTeamId(TEAM_ID)).thenReturn(team);
        when(competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(TEAM_ID, existingId.getCompetitionId()))
                .thenReturn(existing);

        when(sponsorServiceClient.getSponsorBySponsorId(SPONSOR_ID)).thenReturn(sponsor);
        when(facilityServiceClient.getFacilityByFacilityId(FACILITY_ID)).thenReturn(facility);

        doAnswer(inv -> {
            CompetitionRequestModel r = inv.getArgument(0);
            CompetitionIdentifier idArg = inv.getArgument(1);
            return Competition.builder()
                    .competitionIdentifier(idArg)
                    .competitionName(r.getCompetitionName())
                    .competitionDate(r.getCompetitionDate())
                    .competitionStatus(r.getCompetitionStatus())
                    .competitionResult(r.getCompetitionResult())
                    .team(team)
                    .sponsor(sponsor)
                    .facility(facility)
                    .build();
        }).when(competitionRequestMapper)
                .requestModelToEntity(
                        eq(validRequest),
                        eq(existingId),
                        eq(team),
                        eq(sponsor),
                        eq(facility)
                );

        when(competitionRepository.save(any(Competition.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CompetitionResponseModel result =
                competitionService.updateCompetition(TEAM_ID, existingId.getCompetitionId(), validRequest);

        assertNotNull(result);
        assertEquals(existingId.getCompetitionId(), result.getCompetitionId());
        verify(competitionRequestMapper).requestModelToEntity(
                eq(validRequest), eq(existingId), eq(team), eq(sponsor), eq(facility)
        );
        verify(competitionRepository).save(any(Competition.class));
    }

    @Test
    void whenUpdateCompetition_dateTooFar_thenThrow() {
        CompetitionRequestModel far = CompetitionRequestModel.builder()
                .competitionName("X")
                .competitionDate(LocalDate.now().plusYears(2))
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.LOSS)
                .sponsorId(SPONSOR_ID)
                .facilityId(FACILITY_ID)
                .build();

        assertThrows(CompetitionDateTooFarException.class,
                () -> competitionService.updateCompetition(TEAM_ID, "any-id", far));
    }

    @Test
    void whenUpdateCompetition_teamNotFound_thenThrow() {
        when(teamServiceClient.getTeamByTeamId(TEAM_ID)).thenReturn(null);
        assertThrows(InvalidInputException.class,
                () -> competitionService.updateCompetition(TEAM_ID, "any-id", validRequest));
    }

    @Test
    void whenUpdateCompetition_notFound_thenThrow() {
        when(teamServiceClient.getTeamByTeamId(TEAM_ID)).thenReturn(team);
        when(competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(TEAM_ID, "no-id"))
                .thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> competitionService.updateCompetition(TEAM_ID, "no-id", validRequest));
    }

    @Test
    void whenUpdateCompetition_unknownSponsor_thenThrow() {
        CompetitionIdentifier cid = new CompetitionIdentifier();
        Competition existing = Competition.builder()
                .competitionIdentifier(cid)
                .team(team)
                .sponsor(sponsor)
                .facility(facility)
                .build();

        when(teamServiceClient.getTeamByTeamId(TEAM_ID)).thenReturn(team);
        when(competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(TEAM_ID, cid.getCompetitionId()))
                .thenReturn(existing);

        when(sponsorServiceClient.getSponsorBySponsorId(SPONSOR_ID)).thenReturn(null);

        assertThrows(InvalidInputException.class,
                () -> competitionService.updateCompetition(TEAM_ID, cid.getCompetitionId(), validRequest));
    }

    @Test
    void whenUpdateCompetition_unknownFacility_thenThrow() {
        CompetitionIdentifier cid = new CompetitionIdentifier();
        Competition existing = Competition.builder()
                .competitionIdentifier(cid)
                .team(team)
                .sponsor(sponsor)
                .facility(facility)
                .build();

        when(teamServiceClient.getTeamByTeamId(TEAM_ID)).thenReturn(team);
        when(competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(TEAM_ID, cid.getCompetitionId()))
                .thenReturn(existing);

        when(sponsorServiceClient.getSponsorBySponsorId(SPONSOR_ID)).thenReturn(sponsor);
        when(facilityServiceClient.getFacilityByFacilityId(FACILITY_ID)).thenReturn(null);

        assertThrows(InvalidInputException.class,
                () -> competitionService.updateCompetition(TEAM_ID, cid.getCompetitionId(), validRequest));
    }

    @Test
    void whenUpdateCompetition_statusCompleted_triggersSponsorPatch() {
        CompetitionIdentifier cid = new CompetitionIdentifier();
        Competition existing = Competition.builder()
                .competitionIdentifier(cid)
                .team(team)
                .sponsor(sponsor)
                .facility(facility)
                .build();

        validRequest.setCompetitionStatus(CompetitionStatusEnum.COMPLETED);

        when(teamServiceClient.getTeamByTeamId(TEAM_ID)).thenReturn(team);
        when(competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(TEAM_ID, cid.getCompetitionId()))
                .thenReturn(existing);

        when(sponsorServiceClient.getSponsorBySponsorId(SPONSOR_ID)).thenReturn(sponsor);
        when(facilityServiceClient.getFacilityByFacilityId(FACILITY_ID)).thenReturn(facility);

        doAnswer(inv -> Competition.builder()
                .competitionIdentifier(cid)
                .competitionName(validRequest.getCompetitionName())
                .competitionDate(validRequest.getCompetitionDate())
                .competitionStatus(CompetitionStatusEnum.COMPLETED)
                .competitionResult(validRequest.getCompetitionResult())
                .team(team)
                .sponsor(sponsor)
                .facility(facility)
                .build()
        ).when(competitionRequestMapper)
                .requestModelToEntity(eq(validRequest), eq(cid), eq(team), eq(sponsor), eq(facility));

        when(competitionRepository.save(any(Competition.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(sponsorServiceClient.patchSponsorLevelBySponsorId(eq(SPONSOR_ID), anyString()))
                .thenReturn(sponsor);

        competitionService.updateCompetition(TEAM_ID, cid.getCompetitionId(), validRequest);

        verify(sponsorServiceClient, times(1))
                .patchSponsorLevelBySponsorId(eq(SPONSOR_ID), anyString());
        verify(competitionRepository, times(2)).save(any(Competition.class));
    }


    @Test
    void whenDeleteCompetition_happyPath_thenSoftDeleteAndPatchSponsor() {
        CompetitionIdentifier cid = new CompetitionIdentifier();
        Competition comp = Competition.builder()
                .competitionIdentifier(cid)
                .team(team)
                .sponsor(sponsor)
                .facility(facility)
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.DRAW)
                .build();

        when(competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(TEAM_ID, cid.getCompetitionId()))
                .thenReturn(comp);

        when(sponsorServiceClient.patchSponsorLevelBySponsorId(eq(SPONSOR_ID), eq("NONE")))
                .thenReturn(
                        SponsorModel.builder()
                                .sponsorId(SPONSOR_ID)
                                .sponsorName("Nike")
                                .sponsorLevel("NONE")
                                .sponsorAmount(0.0)
                                .build()
                );
        when(competitionRepository.save(any(Competition.class)))
                .thenAnswer(i -> i.getArgument(0));

        competitionService.deleteCompetition(TEAM_ID, cid.getCompetitionId());

        verify(competitionRepository, times(2)).save(any(Competition.class));
        verify(sponsorServiceClient, times(1))
                .patchSponsorLevelBySponsorId(eq(SPONSOR_ID), eq("NONE"));
    }

    @Test
    void whenDeleteCompetition_notFound_thenThrow() {
        when(competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(TEAM_ID, "no-id"))
                .thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> competitionService.deleteCompetition(TEAM_ID, "no-id"));
    }


    @Test
    void whenGetAllFacilitiesCalled_thenReturnsStubbedList() {
        List<FacilityModel> facList = List.of(
                FacilityModel.builder()
                        .facilityId("fac1")
                        .facilityName("Stadium")
                        .capacity(50000)
                        .location("City")
                        .build()
        );
        when(facilityServiceClient.getAllFacilities()).thenReturn(facList);

        List<FacilityModel> result = facilityServiceClient.getAllFacilities();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("fac1", result.get(0).getFacilityId());
        verify(facilityServiceClient).getAllFacilities();
    }

    @Test
    void whenGetAllSponsorsCalled_thenReturnsStubbedList() {
        List<SponsorModel> sponsorList = List.of(
                SponsorModel.builder()
                        .sponsorId("s1")
                        .sponsorName("Acme")
                        .sponsorLevel("GOLD")
                        .sponsorAmount(10000.0)
                        .build()
        );
        when(sponsorServiceClient.getAllSponsors()).thenReturn(sponsorList);

        List<SponsorModel> result = sponsorServiceClient.getAllSponsors();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("s1", result.get(0).getSponsorId());
        verify(sponsorServiceClient).getAllSponsors();
    }
    @Test
    void whenGetAllTeamsCalled_thenReturnsStubbedList() {
        List<TeamModel> teamList = List.of(
                TeamModel.builder()
                        .teamId("11111111-1111-1111-1111-111111111111")
                        .teamName("Montreal Eagles")
                        .coachName("John Smith")
                        .teamLevel("COLLEGE")
                        .build()
        );
        when(teamServiceClient.getAllTeams()).thenReturn(teamList);

        List<TeamModel> result = teamServiceClient.getAllTeams();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("11111111-1111-1111-1111-111111111111", result.get(0).getTeamId());
        assertEquals("Montreal Eagles", result.get(0).getTeamName());
        verify(teamServiceClient).getAllTeams();
    }




}
