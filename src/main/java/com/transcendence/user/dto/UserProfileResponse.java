package com.transcendence.user.dto;

import com.transcendence.stats.dto.UserStatsDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileResponse {

    private UserDto user;
    private UserStatsDto stats;
    private Object[] achievements; // always empty for now

    @Getter
    @AllArgsConstructor
    public static class UserDto {
        private Long id;
        private String username;
        private String email;
    }
}
