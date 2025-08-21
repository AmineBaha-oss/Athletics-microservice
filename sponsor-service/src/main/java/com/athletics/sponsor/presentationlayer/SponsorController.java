package com.athletics.sponsor.presentationlayer;

import com.athletics.sponsor.businesslayer.SponsorService;
import com.athletics.sponsor.dataaccesslayer.SponsorLevelEnum;
import com.athletics.sponsor.presentationlayer.SponsorRequestModel;
import com.athletics.sponsor.presentationlayer.SponsorResponseModel;
import com.athletics.sponsor.utils.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/sponsors")
public class SponsorController {

    private final SponsorService sponsorService;
    private static final int UUID_LENGTH = 36;

    public SponsorController(SponsorService sponsorService) {
        this.sponsorService = sponsorService;
    }

    @GetMapping
    public ResponseEntity<List<SponsorResponseModel>> getAllSponsors() {
        return ResponseEntity.ok().body(sponsorService.getAllSponsors());
    }

    @GetMapping("/{sponsorId}")
    public ResponseEntity<SponsorResponseModel> getSponsorById(@PathVariable String sponsorId) {
        if (sponsorId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid sponsorId provided: " + sponsorId);
        }
        return ResponseEntity.ok().body(sponsorService.getSponsorById(sponsorId));
    }

    @PostMapping
    public ResponseEntity<SponsorResponseModel> createSponsor(@RequestBody SponsorRequestModel sponsorRequestModel) {
        SponsorResponseModel newSponsor = sponsorService.createSponsor(sponsorRequestModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(newSponsor);
    }

    @PutMapping("/{sponsorId}")
    public ResponseEntity<SponsorResponseModel> updateSponsor(
            @PathVariable String sponsorId,
            @RequestBody SponsorRequestModel sponsorRequestModel) {
        if (sponsorId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid sponsorId provided: " + sponsorId);
        }
        SponsorResponseModel updatedSponsor =
                sponsorService.updateSponsor(sponsorRequestModel, sponsorId);
        return ResponseEntity.ok(updatedSponsor);
    }

    @DeleteMapping("/{sponsorId}")
    public ResponseEntity<Void> deleteSponsor(@PathVariable String sponsorId) {
        if (sponsorId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid sponsorId provided: " + sponsorId);
        }
        sponsorService.deleteSponsor(sponsorId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{sponsorId}/level")
    public ResponseEntity<SponsorResponseModel> updateSponsorLevel(
            @PathVariable String sponsorId,
            @RequestBody String newLevel
    ) {
        if (sponsorId.length() != UUID_LENGTH) {
            throw new InvalidInputException("Invalid sponsorId provided: " + sponsorId);
        }

        SponsorLevelEnum levelEnum;
        try {
            levelEnum = SponsorLevelEnum.valueOf(newLevel.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidInputException("Invalid sponsor level: " + newLevel);
        }

        SponsorResponseModel updated =
                sponsorService.patchSponsorLevel(sponsorId, levelEnum);

        return ResponseEntity.ok(updated);
    }
}