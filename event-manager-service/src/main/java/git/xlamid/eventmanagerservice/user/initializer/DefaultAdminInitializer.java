package git.xlamid.eventmanagerservice.user.initializer;

import git.xlamid.eventmanagerservice.user.entity.UserEntity;
import git.xlamid.eventmanagerservice.user.model.enums.UserRole;
import git.xlamid.eventmanagerservice.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DefaultAdminInitializer {

    private final String adminLogin;
    private final String adminPassword;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DefaultAdminInitializer(@Value("${default.admin.login}") String adminLogin,
                                   @Value("${default.admin.password}") String adminPassword,
                                   UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        this.adminLogin = adminLogin;
        this.adminPassword = adminPassword;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    @Transactional
    public void initDefaultAdmin() {
        if (userRepository.existsByLogin(adminLogin)) {
            return;
        }
        userRepository.save(new UserEntity(
                null,
                adminLogin,
                30,
                passwordEncoder.encode(adminPassword),
                UserRole.ADMIN.name()
        ));
        log.info("Default admin has been created");
    }
}