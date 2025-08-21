package com.athletics.team.dataaccesslayer.Team;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "teamId", column = @Column(name = "team_id"))
    })
    private TeamIdentifier teamIdentifier;

    private String teamName;
    private String coachName;

    @Enumerated(EnumType.STRING)
    private TeamLevelEnum teamLevel;
}
