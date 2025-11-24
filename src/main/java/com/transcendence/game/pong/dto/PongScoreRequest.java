package com.transcendence.game.pong.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PongScoreRequest {

    private Integer score;

    @JsonProperty("opponentScore")
    private Integer opponentScore;

    private String winner;   // "player" or "opponent"

    @JsonProperty("xpEarned")
    private Integer xpEarned;

    private Integer duration;

    @JsonProperty("isPerfectGame")
    private Boolean isPerfectGame;
}
