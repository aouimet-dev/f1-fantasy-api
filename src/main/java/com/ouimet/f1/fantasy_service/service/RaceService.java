package com.ouimet.f1.fantasy_service.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ouimet.f1.fantasy_service.dto.CreateRaceDto;
import com.ouimet.f1.fantasy_service.entity.Race;
import com.ouimet.f1.fantasy_service.repository.RaceRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour gérer les courses
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RaceService {

    private final RaceRepository raceRepository;

    /**
     * Récupérer toutes les courses triées par raceNumber
     */
    public List<Race> getAllRacesSorted() {
        log.debug("Fetching all races sorted by raceNumber");
        return raceRepository.findAllByOrderByRaceNumberAsc();
    }

    /**
     * Récupérer courses filtrées par statut
     */
    public List<Race> getRacesByCompleted(Boolean completed) {
        log.debug("Fetching races by completed status: {}", completed);
        return raceRepository.findByIsCompletedOrderByRaceNumberAsc(completed);
    }

    /**
     * Récupérer une course par ID
     */
    public Optional<Race> getRaceById(UUID raceId) {
        log.debug("Fetching race {}", raceId);
        return raceRepository.findById(raceId);
    }

    /**
     * Créer une nouvelle course
     * 
     * @throw IllegalArgumentException si raceNumber déjà existe
     */
    public Race createRace(CreateRaceDto dto) {
        log.info("Creating race: {} (number {})", dto.getRaceName(), dto.getRaceNumber());

        // Vérifier que raceNumber est unique
        if (raceRepository.existsByRaceNumber(dto.getRaceNumber())) {
            throw new IllegalArgumentException("Race number " + dto.getRaceNumber() + " already exists");
        }

        Race race = new Race();
        race.setRaceNumber(dto.getRaceNumber());
        race.setRaceName(dto.getRaceName());
        race.setCircuitName(dto.getCircuitName());
        race.setCountry(dto.getCountry());
        race.setRaceDate(dto.getRaceDate());
        race.setIsCompleted(false);

        return raceRepository.save(race);
    }
}
