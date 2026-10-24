package com.swapnil.cricbuzz.repository;

import com.swapnil.cricbuzz.entity.Player;
import com.swapnil.cricbuzz.entity.PlayerRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    @EntityGraph(attributePaths = "team")
    @Query(value = "select p from Player p where (:teamId is null or p.team.id = :teamId) "
            + "and (:role is null or p.role = :role)",
            countQuery = "select count(p) from Player p where (:teamId is null or p.team.id = :teamId) "
                    + "and (:role is null or p.role = :role)")
    Page<Player> search(@Param("teamId") Long teamId, @Param("role") PlayerRole role, Pageable pageable);

    @EntityGraph(attributePaths = "team")
    Optional<Player> findWithTeamById(Long id);
}
