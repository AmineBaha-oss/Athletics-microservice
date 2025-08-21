package com.athletics.team.presentationlayer.Team;

import com.athletics.team.dataaccesslayer.Team.TeamLevelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamRequestModel {
    private String teamId;
    private String teamName;
    private String coachName;
    private TeamLevelEnum teamLevel;
}
