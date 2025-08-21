package com.athletics.apigateway.businesslayer.team;

import com.athletics.apigateway.domainclientlayer.team.TeamServiceClient;
import com.athletics.apigateway.presentationlayer.team.AthleteRequestModel;
import com.athletics.apigateway.presentationlayer.team.AthleteResponseModel;
import com.athletics.apigateway.presentationlayer.team.TeamRequestModel;
import com.athletics.apigateway.presentationlayer.team.TeamResponseModel;
import com.athletics.apigateway.presentationlayer.team.AthleteController;
import com.athletics.apigateway.presentationlayer.team.TeamController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@Service
public class TeamServiceImpl implements TeamService {

    private final TeamServiceClient teamServiceClient;

    public TeamServiceImpl(TeamServiceClient teamServiceClient) {
        this.teamServiceClient = teamServiceClient;
    }


    @Override
    public List<TeamResponseModel> getAllTeams() {
        log.debug("Business Layer: getAllTeams() called");
        List<TeamResponseModel> teams = teamServiceClient.getAllTeams();
        for (TeamResponseModel team : teams) {
            addTeamHateoasLinks(team);
        }
        return teams;
    }

    @Override
    public TeamResponseModel getTeamById(String teamId) {
        log.debug("Business Layer: getTeamById({}) called", teamId);
        TeamResponseModel team = teamServiceClient.getTeamById(teamId);
        return addTeamHateoasLinks(team);
    }

    @Override
    public TeamResponseModel createTeam(TeamRequestModel teamRequestModel) {
        log.debug("Business Layer: createTeam() called");
        TeamResponseModel newTeam = teamServiceClient.createTeam(teamRequestModel);
        return addTeamHateoasLinks(newTeam);
    }

    @Override
    public TeamResponseModel updateTeam(String teamId, TeamRequestModel teamRequestModel) {
        log.debug("Business Layer: updateTeam({}) called", teamId);
        TeamResponseModel updatedTeam = teamServiceClient.updateTeam(teamId, teamRequestModel);
        return addTeamHateoasLinks(updatedTeam);
    }

    @Override
    public TeamResponseModel deleteTeam(String teamId) {
        log.debug("Business Layer: deleteTeam({}) called", teamId);
        return teamServiceClient.deleteTeam(teamId);
    }


    @Override
    public List<AthleteResponseModel> getAllAthletes(String teamId) {
        log.debug("Business Layer: getAllAthletes({}) called", teamId);
        List<AthleteResponseModel> athletes = teamServiceClient.getAllAthletesForTeam(teamId);
        for (AthleteResponseModel athlete : athletes) {
            addAthleteHateoasLinks(teamId, athlete);
        }
        return athletes;
    }

    @Override
    public AthleteResponseModel getAthleteById(String teamId, String athleteId) {
        log.debug("Business Layer: getAthleteById({}, {}) called", teamId, athleteId);
        AthleteResponseModel athlete = teamServiceClient.getAthleteById(teamId, athleteId);
        return addAthleteHateoasLinks(teamId, athlete);
    }

    @Override
    public AthleteResponseModel createAthlete(String teamId, AthleteRequestModel athleteRequestModel) {
        log.debug("Business Layer: createAthlete({}, ...) called", teamId);
        AthleteResponseModel newAthlete = teamServiceClient.createAthleteForTeam(teamId, athleteRequestModel);
        return addAthleteHateoasLinks(teamId, newAthlete);
    }

    @Override
    public AthleteResponseModel updateAthlete(String teamId, String athleteId, AthleteRequestModel athleteRequestModel) {
        log.debug("Business Layer: updateAthlete({}, {}) called", teamId, athleteId);
        AthleteResponseModel updatedAthlete = teamServiceClient.updateAthleteForTeam(teamId, athleteId, athleteRequestModel);
        return addAthleteHateoasLinks(teamId, updatedAthlete);
    }

    @Override
    public void deleteAthlete(String teamId, String athleteId) {
        log.debug("Business Layer: deleteAthlete({}, {}) called", teamId, athleteId);
        teamServiceClient.deleteAthleteForTeam(teamId, athleteId);
    }


    private TeamResponseModel addTeamHateoasLinks(TeamResponseModel team) {
        Link selfLink = linkTo(methodOn(TeamController.class)
                .getTeamById(team.getTeamId()))
                .withSelfRel();
        team.add(selfLink);

        Link allTeamsLink = linkTo(methodOn(TeamController.class)
                .getAllTeams())
                .withRel("allTeams");
        team.add(allTeamsLink);

        return team;
    }

    private AthleteResponseModel addAthleteHateoasLinks(String teamId, AthleteResponseModel athlete) {
        Link selfLink = linkTo(methodOn(AthleteController.class)
                .getAthleteById(teamId, athlete.getAthleteId()))
                .withSelfRel();
        athlete.add(selfLink);

        Link allAthletesLink = linkTo(methodOn(AthleteController.class)
                .getAllAthletes(teamId))
                .withRel("allAthletes");
        athlete.add(allAthletesLink);

        return athlete;
    }
}
