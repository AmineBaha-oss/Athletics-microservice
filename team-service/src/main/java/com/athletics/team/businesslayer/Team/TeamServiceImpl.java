package com.athletics.team.businesslayer.Team;


import com.athletics.team.dataaccesslayer.Team.Team;
import com.athletics.team.dataaccesslayer.Team.TeamIdentifier;
import com.athletics.team.dataaccesslayer.Team.TeamRepository;
import com.athletics.team.mappinglayer.team.TeamRequestMapper;
import com.athletics.team.mappinglayer.team.TeamResponseMapper;
import com.athletics.team.presentationlayer.Team.TeamRequestModel;
import com.athletics.team.presentationlayer.Team.TeamResponseModel;
import com.athletics.team.utils.exceptions.DuplicateTeamException;
import com.athletics.team.utils.exceptions.NotFoundException;
import com.athletics.team.utils.exceptions.TeamNameLengthExceededException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamResponseMapper teamResponseMapper;
    private final TeamRequestMapper teamRequestMapper;

    public TeamServiceImpl(TeamRepository teamRepository,
                           TeamResponseMapper teamResponseMapper,
                           TeamRequestMapper teamRequestMapper) {
        this.teamRepository = teamRepository;
        this.teamResponseMapper = teamResponseMapper;
        this.teamRequestMapper = teamRequestMapper;
    }

    @Override
    public List<TeamResponseModel> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        return teamResponseMapper.entityListToResponseModelList(teams);
    }

    @Override
    public TeamResponseModel getTeamById(String teamId) {
        Team foundTeam = teamRepository.findByTeamIdentifier_TeamId(teamId);
        if (foundTeam == null) {
            throw new NotFoundException("No team found with ID: " + teamId);
        }
        return teamResponseMapper.entityToResponseModel(foundTeam);
    }

    @Override
    public TeamResponseModel createTeam(TeamRequestModel teamRequestModel) {

        if (teamRequestModel.getTeamName() != null
                && teamRequestModel.getTeamName().length() > 30) {
            throw new TeamNameLengthExceededException("Team name cannot exceed 30 characters");
        }
        if (teamRepository.findByTeamIdentifier_TeamId(teamRequestModel.getTeamId()) != null) {
            throw new DuplicateTeamException("Team with ID already exists: " + teamRequestModel.getTeamId());
        }

        Team newTeam = teamRequestMapper.requestModelToEntity(
                teamRequestModel,
                new TeamIdentifier(teamRequestModel.getTeamId())
        );
        Team savedTeam = teamRepository.save(newTeam);
        return teamResponseMapper.entityToResponseModel(savedTeam);
    }


    @Override
    public TeamResponseModel updateTeam(TeamRequestModel teamRequestModel, String teamId) {
        if (teamRequestModel.getTeamName() != null
                && teamRequestModel.getTeamName().length() > 30) {
            throw new TeamNameLengthExceededException("Team name cannot exceed 30 characters");
        }
        Team existingTeam = teamRepository.findByTeamIdentifier_TeamId(teamId);
        if (existingTeam == null) {
            throw new NotFoundException("No team found with ID: " + teamId);
        }

        TeamIdentifier identifier = existingTeam.getTeamIdentifier();

        Team updatedTeam = teamRequestMapper.requestModelToEntity(teamRequestModel, identifier);
        updatedTeam.setId(existingTeam.getId());

        Team savedTeam = teamRepository.save(updatedTeam);
        return teamResponseMapper.entityToResponseModel(savedTeam);
    }

    @Override
    public void deleteTeam(String teamId) {
        Team existingTeam = teamRepository.findByTeamIdentifier_TeamId(teamId);
        if (existingTeam == null) {
            throw new NotFoundException("No team found with ID: " + teamId);
        }
        teamRepository.delete(existingTeam);
    }
}
