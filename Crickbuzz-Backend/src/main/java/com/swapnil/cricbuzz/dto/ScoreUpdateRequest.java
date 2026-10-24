package com.swapnil.cricbuzz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ScoreUpdateRequest(

        @NotNull
        @Valid
        ScoreCard team1,

        @NotNull
        @Valid
        ScoreCard team2

) {
}