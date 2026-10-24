package com.swapnil.cricbuzz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches", indexes = @Index(name = "idx_match_status", columnList = "status"))
@Getter @Setter @NoArgsConstructor
public class CricketMatch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team1_id", nullable = false)
    private Team team1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team2_id", nullable = false)
    private Team team2;

    @Column(nullable = false, length = 150)
    private String venue;

    @Column(nullable = false)
    private LocalDateTime matchDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MatchType matchType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private MatchStatus status = MatchStatus.UPCOMING;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "runs", column = @Column(name = "team1_runs")),
            @AttributeOverride(name = "wickets", column = @Column(name = "team1_wickets")),
            @AttributeOverride(name = "overs", column = @Column(name = "team1_overs"))})
    private Score team1Score = new Score();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "runs", column = @Column(name = "team2_runs")),
            @AttributeOverride(name = "wickets", column = @Column(name = "team2_wickets")),
            @AttributeOverride(name = "overs", column = @Column(name = "team2_overs"))})
    private Score team2Score = new Score();

    @Column(length = 200)
    private String result;
}
