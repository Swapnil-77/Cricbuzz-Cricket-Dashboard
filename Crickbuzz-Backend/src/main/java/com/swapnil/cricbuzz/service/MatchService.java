package com.swapnil.cricbuzz.service;

import com.swapnil.cricbuzz.dto.*;
import com.swapnil.cricbuzz.entity.CricketMatch;
import com.swapnil.cricbuzz.entity.MatchStatus;
import com.swapnil.cricbuzz.entity.Score;
import com.swapnil.cricbuzz.exception.BadRequestException;
import com.swapnil.cricbuzz.exception.ConflictException;
import com.swapnil.cricbuzz.exception.ResourceNotFoundException;
import com.swapnil.cricbuzz.repository.MatchRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MatchService {

    private final MatchRepository repo;
    private final TeamService teamService;

    public MatchService(
            MatchRepository repo,
            TeamService teamService) {

        this.repo = repo;
        this.teamService = teamService;
    }

    // CREATE MATCH
    @Transactional
    public MatchResponse create(MatchRequest req) {

        validateTeams(req);

        CricketMatch match = new CricketMatch();

        match.setTeam1(teamService.find(req.team1Id()));
        match.setTeam2(teamService.find(req.team2Id()));

        match.setVenue(req.venue().trim());
        match.setMatchDate(req.matchDate());
        match.setMatchType(req.matchType());

        match.setStatus(MatchStatus.UPCOMING);

        return MatchResponse.from(repo.save(match));
    }

    // GET MATCHES
    public Page<MatchResponse> list(
            MatchStatus status,
            Pageable pageable) {

        Page<CricketMatch> page;

        if (status == null) {
            page = repo.findAll(pageable);
        } else {
            page = repo.findByStatus(status, pageable);
        }

        return page.map(MatchResponse::from);
    }

    // GET MATCH BY ID
    public MatchResponse get(Long id) {
        return MatchResponse.from(find(id));
    }

    // EDIT MATCH DETAILS
    @Transactional
    public MatchResponse update(Long id, MatchRequest req) {

        CricketMatch match = find(id);

        validateTeams(req);

        match.setTeam1(teamService.find(req.team1Id()));
        match.setTeam2(teamService.find(req.team2Id()));

        match.setVenue(req.venue().trim());
        match.setMatchDate(req.matchDate());
        match.setMatchType(req.matchType());

        // Status and scores remain unchanged.
        return MatchResponse.from(repo.save(match));
    }

    // UPDATE SCORE
    @Transactional
    public MatchResponse updateScore(
            Long id,
            ScoreUpdateRequest req) {

        CricketMatch match = find(id);

        if (match.getStatus() != MatchStatus.LIVE) {
            throw new ConflictException(
                    "Score can only be updated while the match is LIVE"
            );
        }

        match.setTeam1Score(toScore(req.team1()));
        match.setTeam2Score(toScore(req.team2()));

        return MatchResponse.from(repo.save(match));
    }

    // UPDATE STATUS
    @Transactional
    public MatchResponse updateStatus(
            Long id,
            MatchStatus next) {

        CricketMatch match = find(id);

        boolean valid =
                (match.getStatus() == MatchStatus.UPCOMING
                        && next == MatchStatus.LIVE)
                        ||
                        (match.getStatus() == MatchStatus.LIVE
                                && next == MatchStatus.COMPLETED);

        if (!valid) {
            throw new ConflictException(
                    "Invalid status transition: "
                            + match.getStatus()
                            + " -> "
                            + next
            );
        }

        match.setStatus(next);

        if (next == MatchStatus.COMPLETED) {
            match.setResult(computeResult(match));
        }

        return MatchResponse.from(repo.save(match));
    }

    // DELETE MATCH
    @Transactional
    public void delete(Long id) {
        repo.delete(find(id));
    }

    // VALIDATE TEAMS
    private void validateTeams(MatchRequest req) {

        if (req.team1Id().equals(req.team2Id())) {
            throw new BadRequestException(
                    "A team cannot play against itself"
            );
        }
    }

    // FIND MATCH OR THROW EXCEPTION
    private CricketMatch find(Long id) {
        return repo.findWithTeamsById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Match", id)
                );
    }

    // CONVERT SCORE DTO TO ENTITY
    private Score toScore(ScoreCard card) {
        return new Score(
                card.runs(),
                card.wickets(),
                card.overs()
        );
    }

    // CALCULATE RESULT
    private String computeResult(CricketMatch match) {

        int runs1 = match.getTeam1Score().getRuns();
        int runs2 = match.getTeam2Score().getRuns();

        if (runs1 == runs2) {
            return "Match tied";
        }

        var winner = runs1 > runs2
                ? match.getTeam1()
                : match.getTeam2();

        return winner.getName()
                + " won by "
                + Math.abs(runs1 - runs2)
                + " runs";
    }
}