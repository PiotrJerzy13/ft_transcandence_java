package com.transcendence.game.pong.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PongHistoryResponse {

    private List<PongHistoryItemDto> history;

    public PongHistoryResponse(List<PongHistoryItemDto> history) {
        this.history = history;
    }
}


