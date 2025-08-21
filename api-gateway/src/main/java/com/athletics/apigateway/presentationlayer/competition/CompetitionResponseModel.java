package com.athletics.apigateway.presentationlayer.competition;


import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class CompetitionResponseModel extends RepresentationModel<CompetitionResponseModel> {
    private String competitionId;
    private String competitionName;
    private LocalDate competitionDate;
    private String competitionStatus;
    private String competitionResult;

    private String teamId;
    private String teamName;
    private String coachName;
    private String teamLevel;

    private String sponsorId;
    private String sponsorName;
    private String sponsorLevel;
    private Double sponsorAmount;

    private String facilityId;
    private String facilityName;
    private Integer capacity;
    private String location;
}
