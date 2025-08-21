package com.athletics.sponsor.businesslayer;



import com.athletics.sponsor.dataaccesslayer.Sponsor;
import com.athletics.sponsor.dataaccesslayer.SponsorIdentifier;
import com.athletics.sponsor.dataaccesslayer.SponsorLevelEnum;
import com.athletics.sponsor.dataaccesslayer.SponsorRepository;
import com.athletics.sponsor.mappinglayer.SponsorResponseMapper;
import com.athletics.sponsor.mappinglayer.SponsorRequestMapper;

import com.athletics.sponsor.presentationlayer.SponsorRequestModel;
import com.athletics.sponsor.presentationlayer.SponsorResponseModel;
import com.athletics.sponsor.utils.exceptions.InsufficientSponsorAmountException;
import com.athletics.sponsor.utils.exceptions.InvalidInputException;
import com.athletics.sponsor.utils.exceptions.NotFoundException;
import com.athletics.sponsor.utils.exceptions.SponsorIdentityClashException;

import java.util.List;

@Service
public class SponsorServiceImpl implements SponsorService {

    private final SponsorRepository sponsorRepository;
    private final SponsorResponseMapper sponsorResponseMapper;
    private final SponsorRequestMapper sponsorRequestMapper;

    public SponsorServiceImpl(SponsorRepository sponsorRepository,
                              SponsorResponseMapper sponsorResponseMapper,
                              SponsorRequestMapper sponsorRequestMapper) {
        this.sponsorRepository = sponsorRepository;
        this.sponsorResponseMapper = sponsorResponseMapper;
        this.sponsorRequestMapper = sponsorRequestMapper;
    }

    @Override
    public List<SponsorResponseModel> getAllSponsors() {
        List<Sponsor> sponsors = sponsorRepository.findAll();
        return sponsorResponseMapper.entityListToResponseModelList(sponsors);
    }

    @Override
    public SponsorResponseModel getSponsorById(String sponsorId) {
        Sponsor sponsor = sponsorRepository.findBySponsorIdentifier_SponsorId(sponsorId);
        if (sponsor == null) {
            throw new NotFoundException("Sponsor not found with ID: " + sponsorId);
        }
        return sponsorResponseMapper.entityToResponseModel(sponsor);
    }

    @Override
    public SponsorResponseModel createSponsor(SponsorRequestModel sponsorRequestModel) {

        // Check for existing sponsor
        if (sponsorRepository.findBySponsorIdentifier_SponsorId(sponsorRequestModel.getSponsorId()) != null) {
            throw new SponsorIdentityClashException(
                    "A sponsor with the ID " + sponsorRequestModel.getSponsorId() + " already exists, causing an identity clash."
            );
        }

        // New check for blank name -> throw InvalidInputException
        if (sponsorRequestModel.getSponsorName() == null || sponsorRequestModel.getSponsorName().trim().isEmpty()) {
            throw new InvalidInputException("Sponsor name cannot be empty.");
        }

        // Existing check for insufficient amount
        double minimumRequiredAmount = 1000.00;
        if (sponsorRequestModel.getSponsorAmount() < minimumRequiredAmount) {
            throw new InsufficientSponsorAmountException(
                    "The sponsor amount is too low. Minimum required amount is " + minimumRequiredAmount
            );
        }


        Sponsor newSponsor = sponsorRequestMapper.requestModelToEntity(
                sponsorRequestModel,
                new SponsorIdentifier(sponsorRequestModel.getSponsorId())
        );
        Sponsor savedSponsor = sponsorRepository.save(newSponsor);
        return sponsorResponseMapper.entityToResponseModel(savedSponsor);
    }

    @Override
    public SponsorResponseModel updateSponsor(SponsorRequestModel sponsorRequestModel, String sponsorId) {
        Sponsor existingSponsor = sponsorRepository.findBySponsorIdentifier_SponsorId(sponsorId);
        if (existingSponsor == null) {
            throw new NotFoundException("Sponsor not found with ID: " + sponsorId);
        }

        // — NEW: enforce minimum amount on update —
        double minimumRequiredAmount = 1000.00;
        if (sponsorRequestModel.getSponsorAmount() < minimumRequiredAmount) {
            throw new InsufficientSponsorAmountException(
                    "The sponsor amount is too low. Minimum required amount is " + minimumRequiredAmount
            );
        }

        SponsorIdentifier sponsorIdentifier = existingSponsor.getSponsorIdentifier();
        Sponsor updatedSponsor = sponsorRequestMapper.requestModelToEntity(sponsorRequestModel, sponsorIdentifier);
        updatedSponsor.setId(existingSponsor.getId());

        Sponsor savedSponsor = sponsorRepository.save(updatedSponsor);
        return sponsorResponseMapper.entityToResponseModel(savedSponsor);
    }


    @Override
    public SponsorResponseModel patchSponsorLevel(String sponsorId, SponsorLevelEnum newLevel) {
        Sponsor sponsor = sponsorRepository.findBySponsorIdentifier_SponsorId(sponsorId);
        if (sponsor == null) {
            throw new NotFoundException("Sponsor not found with ID: " + sponsorId);
        }
        sponsor.setSponsorLevel(newLevel);
        Sponsor saved = sponsorRepository.save(sponsor);
        return sponsorResponseMapper.entityToResponseModel(saved);
    }

    @Override
    public void deleteSponsor(String sponsorId) {
        Sponsor existingSponsor = sponsorRepository.findBySponsorIdentifier_SponsorId(sponsorId);
        if (existingSponsor == null) {
            throw new NotFoundException("Sponsor not found with ID: " + sponsorId);
        }
        sponsorRepository.delete(existingSponsor);
    }
}
