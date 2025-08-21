package com.athletics.competition.businesslayer;

import com.athletics.competition.dataaccesslayer.Competition;
import com.athletics.competition.dataaccesslayer.CompetitionIdentifier;
import com.athletics.competition.dataaccesslayer.CompetitionRepository;
import com.athletics.competition.dataaccesslayer.CompetitionStatusEnum;
import com.athletics.competition.domainclientLayer.facility.FacilityModel;
import com.athletics.competition.domainclientLayer.facility.FacilityServiceClient;
import com.athletics.competition.domainclientLayer.sponsor.SponsorLevelEnum;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CompetitionServiceImpl implements CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final TeamServiceClient teamServiceClient;
    private final SponsorServiceClient sponsorServiceClient;
    private final FacilityServiceClient facilityServiceClient;
    private final CompetitionRequestMapper competitionRequestMapper;
    private final CompetitionResponseMapper competitionResponseMapper;

    public CompetitionServiceImpl(CompetitionRepository competitionRepository,
                                  TeamServiceClient teamServiceClient,
                                  SponsorServiceClient sponsorServiceClient,
                                  FacilityServiceClient facilityServiceClient,
                                  CompetitionRequestMapper competitionRequestMapper,
                                  CompetitionResponseMapper competitionResponseMapper) {
        this.competitionRepository     = competitionRepository;
        this.teamServiceClient         = teamServiceClient;
        this.sponsorServiceClient      = sponsorServiceClient;
        this.facilityServiceClient     = facilityServiceClient;
        this.competitionRequestMapper  = competitionRequestMapper;
        this.competitionResponseMapper = competitionResponseMapper;
    }

    @Override
    public List<CompetitionResponseModel> getAllCompetitions(String teamId) {
        teamServiceClient.getTeamByTeamId(teamId);

        List<CompetitionResponseModel> results = new ArrayList<>();
        for (Competition comp : competitionRepository.findAllByTeam_TeamId(teamId)) {
            results.add(
                    competitionResponseMapper.competitionEntityToCompetitionResponseModel(comp)
            );
        }
        return results;
    }

    @Override
    public CompetitionResponseModel getCompetitionById(String teamId, String competitionId) {
        teamServiceClient.getTeamByTeamId(teamId);

        Competition comp = competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(teamId, competitionId);
        if (comp == null) {
            throw new NotFoundException(
                    String.format("Competition %s not found for team %s", competitionId, teamId)
            );
        }
        return competitionResponseMapper.competitionEntityToCompetitionResponseModel(comp);
    }

    @Override
    public CompetitionResponseModel createCompetition(String teamId, CompetitionRequestModel requestModel) {
        if (requestModel.getCompetitionDate() != null
                && requestModel.getCompetitionDate().isAfter(LocalDate.now().plusYears(1))) {
            throw new CompetitionDateTooFarException(
                    "The competition date must be within one year of today."
            );
        }

        TeamModel team = teamServiceClient.getTeamByTeamId(teamId);
        if (team == null) {
            throw new InvalidInputException("unknown team id " + teamId);
        }

        SponsorModel sponsor = sponsorServiceClient.getSponsorBySponsorId(requestModel.getSponsorId());
        if (sponsor == null) {
            throw new InvalidInputException("unknown sponsor id " + requestModel.getSponsorId());
        }
        FacilityModel facility = facilityServiceClient.getFacilityByFacilityId(requestModel.getFacilityId());
        if (facility == null) {
            throw new InvalidInputException("unknown facility id " + requestModel.getFacilityId());
        }

        Competition comp = competitionRequestMapper.requestModelToEntity(
                requestModel,
                new CompetitionIdentifier(),
                team, sponsor, facility
        );
        Competition saved = competitionRepository.save(comp);

        if (saved.getCompetitionStatus() == CompetitionStatusEnum.COMPLETED) {
            SponsorModel bumped = sponsorServiceClient.patchSponsorLevelBySponsorId(
                    saved.getSponsor().getSponsorId(),
                    SponsorLevelEnum.PLATINUM.toString()
            );
            saved.setSponsor(bumped);
            saved = competitionRepository.save(saved);
        }

        return competitionResponseMapper.competitionEntityToCompetitionResponseModel(saved);
    }

    @Override
    public CompetitionResponseModel updateCompetition(String teamId,
                                                      String competitionId,
                                                      CompetitionRequestModel requestModel) {
        if (requestModel.getCompetitionDate() != null
                && requestModel.getCompetitionDate().isAfter(LocalDate.now().plusYears(1))) {
            throw new CompetitionDateTooFarException(
                    "The competition date must be within one year of today."
            );
        }
        TeamModel team = teamServiceClient.getTeamByTeamId(teamId);
        if (team == null) {
            throw new InvalidInputException("unknown team id " + teamId);
        }

        Competition existing = competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(teamId, competitionId);
        if (existing == null) {
            throw new NotFoundException("Competition " + competitionId + " not found for team " + teamId);
        }

        SponsorModel sponsor = sponsorServiceClient.getSponsorBySponsorId(requestModel.getSponsorId());
        if (sponsor == null) {
            throw new InvalidInputException("unknown sponsor id " + requestModel.getSponsorId());
        }
        FacilityModel facility = facilityServiceClient.getFacilityByFacilityId(requestModel.getFacilityId());
        if (facility == null) {
            throw new InvalidInputException("unknown facility id " + requestModel.getFacilityId());
        }

        Competition updated = competitionRequestMapper.requestModelToEntity(
                requestModel,
                existing.getCompetitionIdentifier(),
                team, sponsor, facility
        );
        updated.setId(existing.getId());

        Competition saved = competitionRepository.save(updated);

        if (saved.getCompetitionStatus() == CompetitionStatusEnum.COMPLETED) {
            SponsorModel bumped = sponsorServiceClient.patchSponsorLevelBySponsorId(
                    saved.getSponsor().getSponsorId(),
                    SponsorLevelEnum.PLATINUM.toString()
            );
            saved.setSponsor(bumped);
            saved = competitionRepository.save(saved);
        }

        return competitionResponseMapper.competitionEntityToCompetitionResponseModel(saved);
    }

    @Override
    public void deleteCompetition(String teamId, String competitionId) {
        Competition comp = competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(teamId, competitionId);
        if (comp == null) {
            throw new NotFoundException(
                    "Competition " + competitionId + " not found for team " + teamId);
        }

        CompetitionResponseModel response =
                competitionResponseMapper.competitionEntityToCompetitionResponseModel(comp);
        if (response == null || !response.getTeamId().equals(teamId)) {
            throw new InvalidInputException(
                    "Competition " + competitionId + " does not belong to team " + teamId);
        }

        comp.setCompetitionStatus(CompetitionStatusEnum.CANCELLED);
        competitionRepository.save(comp);

        if (!comp.getSponsor().getSponsorLevel().equals(SponsorLevelEnum.NONE.toString())) {
            SponsorModel updatedSponsor = sponsorServiceClient.patchSponsorLevelBySponsorId(
                    comp.getSponsor().getSponsorId(),
                    SponsorLevelEnum.NONE.toString()
            );

            comp.setSponsor(updatedSponsor);
            competitionRepository.save(comp);
        }
    }

}
