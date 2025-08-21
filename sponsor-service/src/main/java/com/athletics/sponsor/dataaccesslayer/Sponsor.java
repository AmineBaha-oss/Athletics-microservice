package com.athletics.sponsor.dataaccesslayer;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "sponsors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sponsor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "sponsorId", column = @Column(name = "sponsor_id"))
    })
    private SponsorIdentifier sponsorIdentifier;

    private String sponsorName;

    @Enumerated(EnumType.STRING)
    private SponsorLevelEnum sponsorLevel;

    private Double sponsorAmount;
}
