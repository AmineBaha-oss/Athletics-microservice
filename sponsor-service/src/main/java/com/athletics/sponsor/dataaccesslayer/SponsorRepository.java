package com.athletics.sponsor.dataaccesslayer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SponsorRepository extends JpaRepository<Sponsor, Integer> {
    Sponsor findBySponsorIdentifier_SponsorId(String sponsorId);
}
