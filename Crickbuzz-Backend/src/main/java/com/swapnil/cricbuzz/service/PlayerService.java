package com.swapnil.cricbuzz.service;

import com.swapnil.cricbuzz.dto.PlayerRequest;
import com.swapnil.cricbuzz.dto.PlayerResponse;
import com.swapnil.cricbuzz.entity.Player;
import com.swapnil.cricbuzz.entity.PlayerRole;
import com.swapnil.cricbuzz.exception.ResourceNotFoundException;
import com.swapnil.cricbuzz.repository.PlayerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerRepository repo;
    private final TeamService teamService;

    public PlayerService(PlayerRepository repo, TeamService teamService) {
        this.repo = repo;
        this.teamService = teamService;
    }

    @Transactional
    public PlayerResponse create(PlayerRequest req) {
        Player p = new Player();
        apply(p, req);
        return PlayerResponse.from(repo.save(p));
    }

    public Page<PlayerResponse> list(Long teamId, PlayerRole role, Pageable pageable) {
        return repo.search(teamId, role, pageable).map(PlayerResponse::from);
    }

    public PlayerResponse get(Long id) { return PlayerResponse.from(find(id)); }

    @Transactional
    public PlayerResponse update(Long id, PlayerRequest req) {
        Player p = find(id);
        apply(p, req);
        return PlayerResponse.from(p);
    }

    @Transactional
    public void delete(Long id) { repo.delete(find(id)); }

    private Player find(Long id) {
        return repo.findWithTeamById(id).orElseThrow(() -> new ResourceNotFoundException("Player", id));
    }

    private void apply(Player p, PlayerRequest r) {
        p.setName(r.name().trim());
        p.setRole(r.role());
        p.setJerseyNumber(r.jerseyNumber());
        p.setTeam(teamService.find(r.teamId()));
    }
}
