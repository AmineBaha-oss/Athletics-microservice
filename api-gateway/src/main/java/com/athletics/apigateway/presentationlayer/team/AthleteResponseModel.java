package com.athletics.apigateway.presentationlayer.team;


import com.athletics.apigateway.domainclientlayer.team.AthleteCategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AthleteResponseModel extends RepresentationModel<AthleteResponseModel> {
    private String athleteId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private AthleteCategoryEnum athleteCategory;
}
