package com.athletics.apigateway.presentationlayer.sponsor;

import com.athletics.apigateway.businesslayer.sponsor.SponsorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/sponsors")
public class SponsorController {

    private final SponsorService sponsorService;

    public SponsorController(SponsorService sponsorService) {
        this.sponsorService = sponsorService;
    }

    @GetMapping
    public ResponseEntity<List<SponsorResponseModel>> getAllSponsors() {
        log.debug("Presentation Layer: getAllSponsors() called");
        List<SponsorResponseModel> sponsors = sponsorService.getAllSponsors();
        return ResponseEntity.status(HttpStatus.OK).body(sponsors);
    }

    @GetMapping("/{sponsorId}")
    public ResponseEntity<SponsorResponseModel> getSponsorById(@PathVariable String sponsorId) {
        log.debug("Presentation Layer: getSponsorById({}) called", sponsorId);
        SponsorResponseModel sponsor = sponsorService.getSponsorById(sponsorId);
        return ResponseEntity.status(HttpStatus.OK).body(sponsor);
    }

    @PostMapping
    public ResponseEntity<SponsorResponseModel> createSponsor(@RequestBody SponsorRequestModel sponsorRequestModel) {
        log.debug("Presentation Layer: createSponsor() called");
        SponsorResponseModel newSponsor = sponsorService.createSponsor(sponsorRequestModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(newSponsor);
    }

    @PutMapping("/{sponsorId}")
    public ResponseEntity<SponsorResponseModel> updateSponsor(@PathVariable String sponsorId,
                                                              @RequestBody SponsorRequestModel sponsorRequestModel) {
        log.debug("Presentation Layer: updateSponsor({}) called", sponsorId);
        SponsorResponseModel updatedSponsor = sponsorService.updateSponsor(sponsorId, sponsorRequestModel);
        return ResponseEntity.ok(updatedSponsor);
    }

    @DeleteMapping("/{sponsorId}")
    public ResponseEntity<Void> deleteSponsor(@PathVariable String sponsorId) {
        log.debug("Presentation Layer: deleteSponsor({}) called", sponsorId);
        sponsorService.deleteSponsor(sponsorId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
