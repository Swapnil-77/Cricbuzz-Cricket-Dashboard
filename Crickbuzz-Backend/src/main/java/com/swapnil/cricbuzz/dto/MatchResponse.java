package com.swapnil.cricbuzz.dto;

import com.swapnil.cricbuzz.entity.CricketMatch;
import com.swapnil.cricbuzz.entity.MatchStatus;
import com.swapnil.cricbuzz.entity.MatchType;

import java.time.LocalDateTime;

public record MatchResponse(
        Long id, Long team1Id, String team1Name, Long team2Id, String team2Name,
        String venue, LocalDateTime matchDate, MatchType matchType, MatchStatus status,
        ScoreCard team1Score, ScoreCard team2Score, String result) {

    public static MatchResponse from(CricketMatch m) {
        return new MatchResponse(m.getId(),
                m.getTeam1().getId(), m.getTeam1().getName(),
                m.getTeam2().getId(), m.getTeam2().getName(),
                m.getVenue(), m.getMatchDate(), m.getMatchType(), m.getStatus(),
                ScoreCard.from(m.getTeam1Score()), ScoreCard.from(m.getTeam2Score()), m.getResult());
    }
}
