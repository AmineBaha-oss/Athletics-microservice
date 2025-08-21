package com.athletics.team.dataaccesslayer.Team;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Integer> {
    Team findByTeamIdentifier_TeamId(String teamId);
}
