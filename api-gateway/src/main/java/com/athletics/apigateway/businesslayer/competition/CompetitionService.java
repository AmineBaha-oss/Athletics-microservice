package com.athletics.apigateway.businesslayer.competition;


import com.athletics.apigateway.presentationlayer.competition.CompetitionRequestModel;
import com.athletics.apigateway.presentationlayer.competition.CompetitionResponseModel;

import java.util.List;


public interface CompetitionService {
    List<CompetitionResponseModel> getAllCompetitions(String teamId);
    CompetitionResponseModel getCompetitionById(String teamId, String compId);
    CompetitionResponseModel createCompetition(String teamId, CompetitionRequestModel request);
    CompetitionResponseModel updateCompetition(String teamId, String compId, CompetitionRequestModel request);
    void deleteCompetition(String teamId, String compId);
}
