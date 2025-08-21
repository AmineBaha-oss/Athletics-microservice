package com.athletics.apigateway.businesslayer.team;

import com.athletics.apigateway.presentationlayer.team.AthleteRequestModel;
import com.athletics.apigateway.presentationlayer.team.AthleteResponseModel;
import com.athletics.apigateway.presentationlayer.team.TeamRequestModel;
import com.athletics.apigateway.presentationlayer.team.TeamResponseModel;

import java.util.List;

public interface TeamService {
    // Team endpoints
    List<TeamResponseModel> getAllTeams();
    TeamResponseModel getTeamById(String teamId);
    TeamResponseModel createTeam(TeamRequestModel teamRequestModel);
    TeamResponseModel updateTeam(String teamId, TeamRequestModel teamRequestModel);
    TeamResponseModel deleteTeam(String teamId);

    List<AthleteResponseModel> getAllAthletes(String teamId);
    AthleteResponseModel getAthleteById(String teamId, String athleteId);
    AthleteResponseModel createAthlete(String teamId, AthleteRequestModel athleteRequestModel);
    AthleteResponseModel updateAthlete(String teamId, String athleteId, AthleteRequestModel athleteRequestModel);
    void deleteAthlete(String teamId, String athleteId);
}
