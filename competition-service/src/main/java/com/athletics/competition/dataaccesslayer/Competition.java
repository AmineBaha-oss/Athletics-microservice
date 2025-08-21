package com.athletics.competition.dataaccesslayer;



import com.athletics.competition.domainclientLayer.facility.FacilityModel;
import com.athletics.competition.domainclientLayer.sponsor.SponsorModel;
import com.athletics.competition.domainclientLayer.team.TeamModel;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "competitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Competition {

    @Id
    private String id;

    private CompetitionIdentifier competitionIdentifier;

    private String competitionName;
    private LocalDate competitionDate;
    private CompetitionStatusEnum competitionStatus;

    private CompetitionResultEnum competitionResult;

    private TeamModel team;
    private SponsorModel sponsor;
    private FacilityModel facility;
}
