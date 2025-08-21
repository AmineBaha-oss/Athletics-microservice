package com.athletics.apigateway.presentationlayer.team;


import com.athletics.apigateway.domainclientlayer.team.AthleteCategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AthleteRequestModel {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private AthleteCategoryEnum athleteCategory;
}
