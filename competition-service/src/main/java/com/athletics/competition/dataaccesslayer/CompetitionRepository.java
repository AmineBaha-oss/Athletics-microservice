package com.athletics.competition.dataaccesslayer;


import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CompetitionRepository extends MongoRepository<Competition, String> {


    List<Competition> findAllByTeam_TeamId(String teamId);

    Competition findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(String teamId, String competitionId);
}
