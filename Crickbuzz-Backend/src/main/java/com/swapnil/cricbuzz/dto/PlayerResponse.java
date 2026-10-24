package com.swapnil.cricbuzz.dto;

import com.swapnil.cricbuzz.entity.Player;
import com.swapnil.cricbuzz.entity.PlayerRole;

public record PlayerResponse(Long id, String name, PlayerRole role, Integer jerseyNumber, Long teamId, String teamName) {
    public static PlayerResponse from(Player p) {
        return new PlayerResponse(p.getId(), p.getName(), p.getRole(), p.getJerseyNumber(),
                p.getTeam().getId(), p.getTeam().getName());
    }
}
