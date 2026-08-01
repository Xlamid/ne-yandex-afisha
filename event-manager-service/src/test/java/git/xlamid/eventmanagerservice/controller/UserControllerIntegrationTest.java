package git.xlamid.eventmanagerservice.controller;

import git.xlamid.eventmanagerservice.AbstractWithContainerTest;
import git.xlamid.eventmanagerservice.user.dto.AuthUserDto;
import git.xlamid.eventmanagerservice.user.dto.RegisterUserDto;
import git.xlamid.eventmanagerservice.user.entity.UserEntity;
import git.xlamid.eventmanagerservice.user.model.enums.UserRole;
import git.xlamid.eventmanagerservice.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class UserControllerIntegrationTest extends AbstractWithContainerTest {

    private static final String BASE_URL = "/users";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void clearDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnCreatedAndSaveToDatabaseDtoForRegisterUser() throws Exception {
        // Arrange
        RegisterUserDto registerDto = new RegisterUserDto(
                "new_user_test",
                "StrongP@ssw0rd!",
                25
        );

        // Act
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.login").value("new_user_test"))
                .andExpect(jsonPath("$.age").value(25))
                .andExpect(jsonPath("$.role").value("USER"));

        // Verify
        List<UserEntity> users = userRepository.findAll();
        assertEquals(1, users.size());
        UserEntity savedUser = users.getFirst();
        assertEquals("new_user_test", savedUser.getLogin());
        assertNotEquals("StrongP@ssw0rd!", savedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches("StrongP@ssw0rd!", savedUser.getPasswordHash()));
    }

    @Test
    void shouldReturnBadRequestWhenLoginAlreadyExistsForRegisterUser() throws Exception {
        // Arrange
        UserEntity existingUser = new UserEntity(
                null,
                "duplicate_login",
                30,
                "hash",
                UserRole.USER.name()
        );
        userRepository.save(existingUser);

        RegisterUserDto registerDto = new RegisterUserDto(
                "duplicate_login",
                "StrongP@ssw0rd!",
                25
        );

        // Act
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnTokenWithValidCredentialsForAuthenticateUser() throws Exception {
        // Arrange
        UserEntity user = new UserEntity(
                null,
                "auth_user",
                30,
                passwordEncoder.encode("ValidP@ssw0rd!1"),
                UserRole.USER.name()
        );
        userRepository.save(user);

        AuthUserDto authDto = new AuthUserDto("auth_user", "ValidP@ssw0rd!1");

        // Act
        mockMvc.perform(post(BASE_URL + "/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void shouldReturnUnauthorizedWithWrongPasswordForAuthenticateUser() throws Exception {
        // Arrange
        UserEntity user = new UserEntity(
                null,
                "auth_user",
                30,
                passwordEncoder.encode("ValidP@ssw0rd!1"),
                UserRole.USER.name()
        );
        userRepository.save(user);

        AuthUserDto authDto = new AuthUserDto("auth_user", "WrongP@ssw0rd!1");

        // Act
        mockMvc.perform(post(BASE_URL + "/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void shouldReturnUserAsAdminForGetUserById() throws Exception {
        // Arrange
        UserEntity user = new UserEntity(
                null,
                "target_user",
                22,
                "hash",
                UserRole.USER.name()
        );
        UserEntity savedUser = userRepository.save(user);

        // Act
        mockMvc.perform(get(BASE_URL + "/" + savedUser.getId()))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.login").value("target_user"))
                .andExpect(jsonPath("$.age").value(22));
    }

    @Test
    @WithMockUser(username = "simple_user", authorities = "USER")
    void shouldReturnForbiddenAsUserForGetUserById() throws Exception {
        // Arrange
        UserEntity user = new UserEntity(
                null,
                "target_user",
                22,
                "hash",
                UserRole.USER.name()
        );
        UserEntity savedUser = userRepository.save(user);

        // Act
        mockMvc.perform(get(BASE_URL + "/" + savedUser.getId()))
                .andDo(print())
                // Assert
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void shouldReturnNotFoundWhenNotFoundForGetUserById() throws Exception {
        // Act
        mockMvc.perform(get(BASE_URL + "/99999"))
                .andDo(print())
                // Assert
                .andExpect(status().isNotFound());
    }
}