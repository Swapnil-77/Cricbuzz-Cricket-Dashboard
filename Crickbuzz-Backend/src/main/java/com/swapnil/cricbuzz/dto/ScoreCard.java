package com.swapnil.cricbuzz.dto;

import com.swapnil.cricbuzz.entity.Score;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ScoreCard(

        @Min(0)
        int runs,

        @Min(0)
        @Max(10)
        int wickets,

        @DecimalMin("0.0")
        double overs

) {

    public static ScoreCard from(Score score) {
        return new ScoreCard(
                score.getRuns(),
                score.getWickets(),
                score.getOvers()
        );
    }
}