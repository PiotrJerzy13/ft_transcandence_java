package com.transcendence.auth;

import com.transcendence.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, String> {


    boolean existsByToken(String token);

    // TODO: Implement scheduled cleanup service to automatically remove expired tokens
    //  This will prevent database bloat by periodically deleting tokens past their expiry date
    //  Consider using @Scheduled annotation with cron expression
    void deleteByExpiryDateBefore(LocalDateTime dateTime);
}
