package com.athletics.team.dataaccesslayer.Team;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class TeamIdentifier {
    private String teamId;

    public TeamIdentifier(String teamId) {
        this.teamId = teamId;
    }
}
