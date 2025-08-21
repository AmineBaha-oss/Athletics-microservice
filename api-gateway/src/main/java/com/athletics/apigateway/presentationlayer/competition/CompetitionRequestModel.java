package com.athletics.apigateway.presentationlayer.competition;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionRequestModel {
    private String competitionName;
    private LocalDate competitionDate;
    private String competitionStatus;
    private String competitionResult;
    private String sponsorId;
    private String facilityId;
}
