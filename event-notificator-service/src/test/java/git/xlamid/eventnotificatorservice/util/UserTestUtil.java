package git.xlamid.eventnotificatorservice.util;

import git.xlamid.eventnotificatorservice.util.model.TestUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class UserTestUtil {

    public static final String ADMIN_LOGIN_1 = "admin1";
    public static final String USER_LOGIN_1 = "user1";
    public static final String USER_LOGIN_2 = "user2";

    private static final Map<String, TestUser> TEST_USERS = Map.of(
            ADMIN_LOGIN_1, new TestUser(1L, ADMIN_LOGIN_1, "ADMIN"),
            USER_LOGIN_1, new TestUser(2L, USER_LOGIN_1, "USER"),
            USER_LOGIN_2, new TestUser(3L, USER_LOGIN_2, "USER")
    );

    private final SecretKey secretKey;

    public UserTestUtil(@Value("${jwt.secret-public-key}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.toUpperCase().getBytes());
    }

    public String getJwtTokenByLogin(String login) {
        TestUser user = getTestUserByLogin(login);
        return Jwts.builder()
                .subject(user.getLogin())
                .claim("id", user.getId())
                .claim("role", user.getRole())
                .signWith(secretKey)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 600_000L))
                .compact();
    }

    private TestUser getTestUserByLogin(String login) {
        TestUser user = TEST_USERS.get(login);
        if (user == null) {
            throw new IllegalArgumentException("Test user with login: " + login + " not found");
        }
        return user;
    }
}