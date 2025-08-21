package com.athletics.facility.dataaccesslayer;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "facilities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    @AttributeOverride(name = "facilityId", column = @Column(name = "FACILITY_ID", unique = true, nullable = false))
    private FacilityIdentifier facilityIdentifier;

    private String facilityName;
    private Integer capacity;
    private String location;
}
