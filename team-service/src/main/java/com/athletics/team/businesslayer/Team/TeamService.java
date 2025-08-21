package com.athletics.team.businesslayer.Team;


import com.athletics.team.presentationlayer.Team.TeamRequestModel;
import com.athletics.team.presentationlayer.Team.TeamResponseModel;

import java.util.List;

public interface TeamService {
    List<TeamResponseModel> getAllTeams();
    TeamResponseModel getTeamById(String teamId);
    TeamResponseModel createTeam(TeamRequestModel teamRequestModel);
    TeamResponseModel updateTeam(TeamRequestModel teamRequestModel, String teamId);
    void deleteTeam(String teamId);

}
