package com.athletics.team.dataaccesslayer.Athlete;


import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.UUID;

@Embeddable
@Getter
public class AthleteIdentifier {
    private String athleteId;

    public AthleteIdentifier() {
        this.athleteId = UUID.randomUUID().toString();
    }
}
