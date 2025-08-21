package com.athletics.competition.presentationlayer;
import com.athletics.competition.dataaccesslayer.CompetitionResultEnum;
import com.athletics.competition.dataaccesslayer.CompetitionStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompetitionRequestModel {
    private String competitionName;
    private LocalDate competitionDate;
    private CompetitionStatusEnum competitionStatus;
    private CompetitionResultEnum competitionResult;
    private String sponsorId;
    private String facilityId;
}
