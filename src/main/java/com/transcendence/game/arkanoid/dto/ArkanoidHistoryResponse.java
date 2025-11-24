package com.transcendence.game.arkanoid.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ArkanoidHistoryResponse {

    private List<ArkanoidHistoryItemDto> history;

    public ArkanoidHistoryResponse(List<ArkanoidHistoryItemDto> history) {
        this.history = history;
    }
}

