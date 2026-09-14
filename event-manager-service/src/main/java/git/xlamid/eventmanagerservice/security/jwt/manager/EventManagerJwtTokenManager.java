package git.xlamid.eventmanagerservice.security.jwt.manager;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class EventManagerJwtTokenManager {

    private final SecretKey secretKey;
    private final Long expirationTime;

    public EventManagerJwtTokenManager(@Value("${jwt.secret-private-key}") String secretKey,
                                       @Value("${jwt.lifetime}") Long expirationTime) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.toUpperCase().getBytes());
        this.expirationTime = expirationTime;
    }

    public String generateToken(String login, Long id, String role) {
        return Jwts.builder()
                .subject(login)
                .claim("id", id)
                .claim("role", role)
                .signWith(secretKey)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .compact();
    }

    public String getLoginFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}