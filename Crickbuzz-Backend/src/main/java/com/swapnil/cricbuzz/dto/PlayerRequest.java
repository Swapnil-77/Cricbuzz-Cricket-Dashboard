package com.swapnil.cricbuzz.dto;

import com.swapnil.cricbuzz.entity.PlayerRole;
import jakarta.validation.constraints.*;

public record PlayerRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull PlayerRole role,
        @Min(0) @Max(999) Integer jerseyNumber,
        @NotNull Long teamId) {
}
