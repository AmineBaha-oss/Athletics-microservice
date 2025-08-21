package com.athletics.competition.businesslayer;


import com.athletics.competition.presentationlayer.CompetitionRequestModel;
import com.athletics.competition.presentationlayer.CompetitionResponseModel;

import java.util.List;

public interface CompetitionService {
    List<CompetitionResponseModel> getAllCompetitions(String teamId);
    CompetitionResponseModel getCompetitionById(String teamId, String competitionId);
    CompetitionResponseModel createCompetition(String teamId, CompetitionRequestModel requestModel);
    CompetitionResponseModel updateCompetition(String teamId, String competitionId, CompetitionRequestModel requestModel);
    void deleteCompetition(String teamId, String competitionId);
}
