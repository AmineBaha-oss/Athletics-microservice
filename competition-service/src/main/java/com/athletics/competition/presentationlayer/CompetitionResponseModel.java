package com.athletics.competition.presentationlayer;
import com.athletics.competition.dataaccesslayer.CompetitionResultEnum;
import com.athletics.competition.dataaccesslayer.CompetitionStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompetitionResponseModel {
    private String competitionId;
    private String competitionName;
    private LocalDate competitionDate;
    private CompetitionStatusEnum competitionStatus;
    private CompetitionResultEnum competitionResult;
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
