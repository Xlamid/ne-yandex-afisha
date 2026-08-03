package git.xlamid.eventmanagerservice.user.service;

import git.xlamid.eventmanagerservice.exception.model.exists.UserExistsException;
import git.xlamid.eventmanagerservice.exception.model.notfound.UserNotFoundException;
import git.xlamid.eventmanagerservice.user.dto.GetUserDto;
import git.xlamid.eventmanagerservice.user.dto.RegisterUserDto;
import git.xlamid.eventmanagerservice.user.entity.UserEntity;
import git.xlamid.eventmanagerservice.user.mapper.UserMapper;
import git.xlamid.eventmanagerservice.user.model.enums.UserRole;
import git.xlamid.eventmanagerservice.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public GetUserDto registerUser(RegisterUserDto dto) {
        validateUserExists(dto.getLogin());
        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        dto.setPassword(hashedPassword);

        return userMapper.entityToGetDto(
                userRepository.save(createUser(dto))
        );
    }

    public GetUserDto getUserById(Long id) {
        return userMapper.entityToGetDto(
                findUserById(id)
        );
    }

    private void validateUserExists(String login) {
        if (userRepository.existsByLogin(login)) {
            throw new UserExistsException("User with login: " + login + " already exists");
        }
    }

    private UserEntity createUser(RegisterUserDto dto) {
        return new UserEntity(
                null,
                dto.getLogin(),
                dto.getAge(),
                dto.getPassword(),
                UserRole.USER.name()
        );
    }

    private UserEntity findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id: " + id + " not found"));
    }
}