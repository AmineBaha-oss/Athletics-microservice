package com.athletics.team.presentationlayer.Athlete;

import com.athletics.team.dataaccesslayer.Athlete.AthleteCategoryEnum;
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
    private String teamId;
}
