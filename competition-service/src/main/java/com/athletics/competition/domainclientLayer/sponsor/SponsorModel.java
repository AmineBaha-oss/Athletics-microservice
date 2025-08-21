package com.athletics.competition.domainclientLayer.sponsor;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class SponsorModel {
    private String sponsorId;
    private String sponsorName;
    private String sponsorLevel;
    private Double sponsorAmount;
}
