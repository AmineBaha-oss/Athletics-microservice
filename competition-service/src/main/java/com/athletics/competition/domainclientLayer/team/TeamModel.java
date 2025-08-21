package com.athletics.competition.domainclientLayer.team;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class TeamModel {
    private String teamId;
    private String teamName;
    private String coachName;
    private String teamLevel;
}
