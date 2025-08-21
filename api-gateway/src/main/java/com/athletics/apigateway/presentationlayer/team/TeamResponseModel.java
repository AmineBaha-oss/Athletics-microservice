package com.athletics.apigateway.presentationlayer.team;


import com.athletics.apigateway.domainclientlayer.team.TeamLevelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamResponseModel extends RepresentationModel<TeamResponseModel> {
    private String teamId;
    private String teamName;
    private String coachName;
    private TeamLevelEnum teamLevel;
}
