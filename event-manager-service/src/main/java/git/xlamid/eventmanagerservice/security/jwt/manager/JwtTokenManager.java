package git.xlamid.eventmanagerservice.security.jwt.manager;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenManager {

    private final SecretKey secretKey;
    private final Long expirationTime;

    public JwtTokenManager(@Value("${jwt.secret-key}") String secretKey,
                           @Value("${jwt.lifetime}") Long expirationTime) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.toUpperCase().getBytes());
        this.expirationTime = expirationTime;
    }

    public String generateToken(String login) {
        return Jwts.builder()
                .subject(login)
                .signWith(secretKey)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .compact();
    }

    public String getLoginFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}