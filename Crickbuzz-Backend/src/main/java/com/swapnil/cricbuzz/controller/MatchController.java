package com.swapnil.cricbuzz.controller;

import com.swapnil.cricbuzz.dto.MatchRequest;
import com.swapnil.cricbuzz.dto.MatchResponse;
import com.swapnil.cricbuzz.dto.ScoreUpdateRequest;
import com.swapnil.cricbuzz.dto.StatusUpdateRequest;
import com.swapnil.cricbuzz.entity.MatchStatus;
import com.swapnil.cricbuzz.service.MatchService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchController {

    private final MatchService service;

    public MatchController(MatchService service) {
        this.service = service;
    }

    // Create a new match
    @PostMapping
    public ResponseEntity<MatchResponse> create(
            @Valid @RequestBody MatchRequest req) {

        MatchResponse created = service.create(req);

        return ResponseEntity.status(201).body(created);
    }

    // Get all matches or filter by status
    @GetMapping
    public PagedModel<MatchResponse> list(
            @RequestParam(required = false) MatchStatus status,
            @PageableDefault(
                    size = 20,
                    sort = "matchDate",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        return new PagedModel<>(service.list(status, pageable));
    }

    // Get match by ID
    @GetMapping("/{id}")
    public MatchResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    // Edit match details
    @PutMapping("/{id}")
    public MatchResponse update(
            @PathVariable Long id,
            @Valid @RequestBody MatchRequest req) {

        return service.update(id, req);
    }

    // Update scores of a LIVE match
    @PatchMapping("/{id}/score")
    public MatchResponse updateScore(
            @PathVariable Long id,
            @Valid @RequestBody ScoreUpdateRequest req) {

        return service.updateScore(id, req);
    }

    // Update match status
    @PatchMapping("/{id}/status")
    public MatchResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest req) {

        return service.updateStatus(id, req.status());
    }

    // Delete match
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}