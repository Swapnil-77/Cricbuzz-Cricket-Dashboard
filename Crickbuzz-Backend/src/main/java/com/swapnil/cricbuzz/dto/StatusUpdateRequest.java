package com.swapnil.cricbuzz.dto;

import com.swapnil.cricbuzz.entity.MatchStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(

        @NotNull
        MatchStatus status

) {
}