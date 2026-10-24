package com.swapnil.cricbuzz.repository;

import com.swapnil.cricbuzz.entity.CricketMatch;
import com.swapnil.cricbuzz.entity.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchRepository extends JpaRepository<CricketMatch, Long> {

    @Override
    @EntityGraph(attributePaths = {"team1", "team2"})
    Page<CricketMatch> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"team1", "team2"})
    Page<CricketMatch> findByStatus(MatchStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"team1", "team2"})
    Optional<CricketMatch> findWithTeamsById(Long id);
}
