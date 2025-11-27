package com.transcendence.game.arkanoid.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ArkanoidHistoryResponse {

    private List<ArkanoidHistoryItemDto> history;
}

