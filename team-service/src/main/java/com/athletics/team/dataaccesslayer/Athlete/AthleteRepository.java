package com.athletics.team.dataaccesslayer.Athlete;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AthleteRepository extends JpaRepository<Athlete, Integer> {
    List<Athlete> findByTeamId(String teamId);
    Athlete findByTeamIdAndAthleteIdentifier_AthleteId(String teamId, String athleteId);
}
