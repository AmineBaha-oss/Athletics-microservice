package com.athletics.team.businesslayer.Athlete;


import com.athletics.team.dataaccesslayer.Athlete.Athlete;
import com.athletics.team.dataaccesslayer.Athlete.AthleteIdentifier;
import com.athletics.team.dataaccesslayer.Athlete.AthleteRepository;
import com.athletics.team.mappinglayer.Athlete.AthleteRequestMapper;
import com.athletics.team.mappinglayer.Athlete.AthleteResponseMapper;
import com.athletics.team.presentationlayer.Athlete.AthleteRequestModel;
import com.athletics.team.presentationlayer.Athlete.AthleteResponseModel;
import com.athletics.team.utils.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AthleteServiceImpl implements AthleteService {

    private final AthleteRepository athleteRepository;
    private final AthleteRequestMapper athleteRequestMapper;
    private final AthleteResponseMapper athleteResponseMapper;

    public AthleteServiceImpl(AthleteRepository athleteRepository,
                              AthleteRequestMapper athleteRequestMapper,
                              AthleteResponseMapper athleteResponseMapper) {
        this.athleteRepository = athleteRepository;
        this.athleteRequestMapper = athleteRequestMapper;
        this.athleteResponseMapper = athleteResponseMapper;
    }

    @Override
    public List<AthleteResponseModel> getAllAthletesForTeam(String teamId) {
        List<Athlete> athletes = athleteRepository.findByTeamId(teamId);
        return athleteResponseMapper.entityListToResponseModelList(athletes);
    }

    @Override
    public AthleteResponseModel getAthleteById(String teamId, String athleteId) {
        Athlete athlete = athleteRepository.findByTeamIdAndAthleteIdentifier_AthleteId(teamId, athleteId);
        if (athlete == null) {
            throw new NotFoundException("Athlete not found with teamId: " + teamId + " and athleteId: " + athleteId);
        }
        return athleteResponseMapper.entityToResponseModel(athlete);
    }

    @Override
    public AthleteResponseModel createAthleteForTeam(String teamId, AthleteRequestModel athleteRequestModel) {
        AthleteIdentifier athleteIdentifier = new AthleteIdentifier();
        Athlete athlete = athleteRequestMapper.requestModelToEntity(athleteRequestModel, athleteIdentifier);
        athlete.setTeamId(teamId);
        Athlete saved = athleteRepository.save(athlete);
        return athleteResponseMapper.entityToResponseModel(saved);
    }

    @Override
    public AthleteResponseModel updateAthleteForTeam(String teamId, String athleteId, AthleteRequestModel athleteRequestModel) {
        Athlete existing = athleteRepository.findByTeamIdAndAthleteIdentifier_AthleteId(teamId, athleteId);
        if (existing == null) {
            throw new NotFoundException("Athlete not found with teamId: " + teamId + " and athleteId: " + athleteId);
        }
        AthleteIdentifier identifier = existing.getAthleteIdentifier();
        Athlete updated = athleteRequestMapper.requestModelToEntity(athleteRequestModel, identifier);
        updated.setId(existing.getId());
        updated.setTeamId(teamId);
        Athlete saved = athleteRepository.save(updated);
        return athleteResponseMapper.entityToResponseModel(saved);
    }

    @Override
    public void deleteAthleteForTeam(String teamId, String athleteId) {
        Athlete existing = athleteRepository.findByTeamIdAndAthleteIdentifier_AthleteId(teamId, athleteId);
        if (existing == null) {
            throw new NotFoundException("Athlete not found with teamId: " + teamId + " and athleteId: " + athleteId);
        }
        athleteRepository.delete(existing);
    }
}
