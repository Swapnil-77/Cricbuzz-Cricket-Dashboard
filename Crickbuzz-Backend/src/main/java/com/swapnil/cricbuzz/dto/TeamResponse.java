package com.swapnil.cricbuzz.dto;

import com.swapnil.cricbuzz.entity.Team;

public record TeamResponse(Long id, String name, String shortName, String country) {
    public static TeamResponse from(Team t) {
        return new TeamResponse(t.getId(), t.getName(), t.getShortName(), t.getCountry());
    }
}
