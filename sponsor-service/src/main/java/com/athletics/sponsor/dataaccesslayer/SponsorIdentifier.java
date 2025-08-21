package com.athletics.sponsor.dataaccesslayer;


import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class SponsorIdentifier {

    private String sponsorId;

    public SponsorIdentifier(String sponsorId) {
        this.sponsorId = sponsorId;
    }
}
