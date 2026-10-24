package com.swapnil.cricbuzz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 10) String shortName,
        @NotBlank @Size(max = 60) String country) {
}
