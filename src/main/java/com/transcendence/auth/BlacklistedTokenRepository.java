package com.transcendence.auth;

import com.transcendence.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, String> {


    boolean existsByToken(String token);

    void deleteByExpiryDateBefore(LocalDateTime dateTime);
}
