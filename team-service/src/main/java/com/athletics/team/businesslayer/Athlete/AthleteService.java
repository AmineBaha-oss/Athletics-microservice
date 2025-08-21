package com.athletics.team.businesslayer.Athlete;


import com.athletics.team.presentationlayer.Athlete.AthleteRequestModel;
import com.athletics.team.presentationlayer.Athlete.AthleteResponseModel;

import java.util.List;

public interface AthleteService {
    List<AthleteResponseModel> getAllAthletesForTeam(String teamId);
    AthleteResponseModel getAthleteById(String teamId, String athleteId);
    AthleteResponseModel createAthleteForTeam(String teamId, AthleteRequestModel athleteRequestModel);
    AthleteResponseModel updateAthleteForTeam(String teamId, String athleteId, AthleteRequestModel athleteRequestModel);
    void deleteAthleteForTeam(String teamId, String athleteId);
}
