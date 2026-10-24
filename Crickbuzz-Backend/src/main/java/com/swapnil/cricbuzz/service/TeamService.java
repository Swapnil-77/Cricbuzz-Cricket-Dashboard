package com.swapnil.cricbuzz.service;

import com.swapnil.cricbuzz.dto.TeamRequest;
import com.swapnil.cricbuzz.dto.TeamResponse;
import com.swapnil.cricbuzz.entity.Team;
import com.swapnil.cricbuzz.exception.ConflictException;
import com.swapnil.cricbuzz.exception.ResourceNotFoundException;
import com.swapnil.cricbuzz.repository.TeamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository repo;

    public TeamService(TeamRepository repo) { this.repo = repo; }

    @Transactional
    public TeamResponse create(TeamRequest req) {
        if (repo.existsByNameIgnoreCase(req.name().trim())) {
            throw new ConflictException("Team already exists: " + req.name());
        }
        Team t = new Team();
        apply(t, req);
        return TeamResponse.from(repo.save(t));
    }

    public Page<TeamResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(TeamResponse::from);
    }

    public TeamResponse get(Long id) { return TeamResponse.from(find(id)); }

    @Transactional
    public TeamResponse update(Long id, TeamRequest req) {
        Team t = find(id);
        if (repo.existsByNameIgnoreCaseAndIdNot(req.name().trim(), id)) {
            throw new ConflictException("Team already exists: " + req.name());
        }
        apply(t, req);
        return TeamResponse.from(t);
    }

    @Transactional
    public void delete(Long id) {
        repo.delete(find(id));
        repo.flush(); // surface FK violations here as 409
    }

    Team find(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Team", id));
    }

    private void apply(Team t, TeamRequest r) {
        t.setName(r.name().trim());
        t.setShortName(r.shortName().trim().toUpperCase());
        t.setCountry(r.country().trim());
    }
}
