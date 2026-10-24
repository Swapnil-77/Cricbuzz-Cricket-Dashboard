package com.swapnil.cricbuzz.controller;

import com.swapnil.cricbuzz.dto.PlayerRequest;
import com.swapnil.cricbuzz.dto.PlayerResponse;
import com.swapnil.cricbuzz.entity.PlayerRole;
import com.swapnil.cricbuzz.service.PlayerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/players")
@Tag(name = "Players")
public class PlayerController {

    private final PlayerService service;

    public PlayerController(PlayerService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<PlayerResponse> create(@Valid @RequestBody PlayerRequest req) {
        PlayerResponse created = service.create(req);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").build(created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public PagedModel<PlayerResponse> list(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) PlayerRole role,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return new PagedModel<>(service.list(teamId, role, pageable));
    }

    @GetMapping("/{id}")
    public PlayerResponse get(@PathVariable Long id) { return service.get(id); }

    @PutMapping("/{id}")
    public PlayerResponse update(@PathVariable Long id, @Valid @RequestBody PlayerRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
