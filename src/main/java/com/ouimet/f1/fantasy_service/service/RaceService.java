package com.ouimet.f1.fantasy_service.service;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.ouimet.f1.fantasy_service.dto.CreateSeasonRacesDto;
import com.ouimet.f1.fantasy_service.dto.RaceDto;
import com.ouimet.f1.fantasy_service.entity.Race;
import com.ouimet.f1.fantasy_service.repository.RaceRepository;

@Service
public class RaceService {

    private final RaceRepository raceRepository;

    public RaceService(RaceRepository raceRepository) {
        this.raceRepository = raceRepository;
    }

    public List<Race> createSeasonRaces(CreateSeasonRacesDto createSeasonRacesDto) {
        List<RaceDto> racesToCreate = createSeasonRacesDto.races();
        List<Race> races = IntStream.range(0, racesToCreate.size())
                .mapToObj(i -> {
                    RaceDto current = racesToCreate.get(i);

                    Race race = new Race();
                    race.setRaceNumber(i+1); // For race 0 to be race 1
                    race.setRaceName(current.raceName());
                    race.setCircuitName(current.circuitName());
                    race.setCountry(current.country());
                    race.setRaceDate(current.raceDate());
                    return race;
                }).toList();

        return raceRepository.saveAll(races);
    }
}
