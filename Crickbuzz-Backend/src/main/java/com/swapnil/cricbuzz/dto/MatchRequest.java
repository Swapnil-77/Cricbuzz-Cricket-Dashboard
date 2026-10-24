package com.swapnil.cricbuzz.dto;

import com.swapnil.cricbuzz.entity.MatchType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record MatchRequest(

        @NotNull
        Long team1Id,

        @NotNull
        Long team2Id,

        @NotBlank
        @Size(max = 150)
        String venue,

        @NotNull
        LocalDateTime matchDate,

        @NotNull
        MatchType matchType

) {
}