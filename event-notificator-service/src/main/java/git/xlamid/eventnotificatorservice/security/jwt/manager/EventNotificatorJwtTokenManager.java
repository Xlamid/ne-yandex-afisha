package git.xlamid.eventnotificatorservice.security.jwt.manager;

import git.xlamid.eventnotificatorservice.security.jwt.model.NotificatorUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class EventNotificatorJwtTokenManager {

    private final SecretKey secretKey;

    public EventNotificatorJwtTokenManager(@Value("${jwt.secret-public-key}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.toUpperCase().getBytes());
    }

    public NotificatorUserDetails getUserDetailsFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return new NotificatorUserDetails(
                claims.get("id", Long.class),
                claims.getSubject(),
                null,
                claims.get("role", String.class)
        );
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}