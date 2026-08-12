package git.xlamid.eventmanagerservice.util;

import git.xlamid.eventmanagerservice.exception.model.notfound.UserNotFoundException;
import git.xlamid.eventmanagerservice.security.jwt.manager.JwtTokenManager;
import git.xlamid.eventmanagerservice.user.entity.UserEntity;
import git.xlamid.eventmanagerservice.user.model.enums.UserRole;
import git.xlamid.eventmanagerservice.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Component
public class UserTestUtil {

    private static final String ADMIN_LOGIN_1 = "admin1";
    private static final String USER_LOGIN_1 = "user1";
    private static final String USER_LOGIN_2 = "user2";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenManager jwtTokenManager;

    @Autowired
    public UserTestUtil(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtTokenManager jwtTokenManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenManager = jwtTokenManager;
    }

    public String getJwtTokenByLogin(String login) {
        return switch (login) {
            case USER_LOGIN_1 -> jwtTokenManager.generateToken(USER_LOGIN_1);
            case ADMIN_LOGIN_1 -> jwtTokenManager.generateToken(ADMIN_LOGIN_1);
            case USER_LOGIN_2 -> jwtTokenManager.generateToken(USER_LOGIN_2);
            default -> throw new UserNotFoundException("Test user with login: " + login + " not found");
        };
    }

    public UserEntity getTestUserByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("Test user with login: " + login + " not found"));
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    protected void initializeTestUsers() {
        if (userRepository.existsByLogin(ADMIN_LOGIN_1)) {
            return;
        }
        createUser(ADMIN_LOGIN_1, 30, "Password1!", UserRole.ADMIN);
        createUser(USER_LOGIN_1, 25, "Password2!", UserRole.USER);
        createUser(USER_LOGIN_2, 27, "Password3!", UserRole.USER);
    }

    private void createUser(String login, Integer age, String password, UserRole role) {
        if (userRepository.existsByLogin(login)) {
            return;
        }
        String hashedPassword = passwordEncoder.encode(password);
        userRepository.save(new UserEntity(
                null,
                login,
                age,
                hashedPassword,
                role.name(),
                new ArrayList<>(),
                new ArrayList<>()
        ));
    }
}