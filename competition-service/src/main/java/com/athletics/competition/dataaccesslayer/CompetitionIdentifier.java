package com.athletics.competition.dataaccesslayer;

import lombok.Getter;
import java.util.UUID;

@Getter
public class CompetitionIdentifier {
    private String competitionId;

    public CompetitionIdentifier() {
        this.competitionId = UUID.randomUUID().toString();
    }
}
