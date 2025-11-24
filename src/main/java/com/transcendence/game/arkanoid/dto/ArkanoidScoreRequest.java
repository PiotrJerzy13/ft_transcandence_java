package com.transcendence.game.arkanoid.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArkanoidScoreRequest {

    private Integer score;

    @JsonProperty("levelReached")
    private Integer levelReached;

    @JsonProperty("xpEarned")
    private Integer xpEarned;

    private Integer duration;

    @JsonProperty("blocksDestroyed")
    private Integer blocksDestroyed;

    @JsonProperty("powerUpsCollected")
    private Integer powerUpsCollected;
}

