package git.xlamid.eventmanagerservice.user.controller;

import git.xlamid.eventmanagerservice.user.service.AuthenticationUserService;
import git.xlamid.eventmanagerservice.user.dto.AuthUserDto;
import git.xlamid.eventmanagerservice.user.dto.GetUserDto;
import git.xlamid.eventmanagerservice.user.dto.GetUserJwtDto;
import git.xlamid.eventmanagerservice.user.dto.RegisterUserDto;
import git.xlamid.eventmanagerservice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationUserService authUserService;

    @PostMapping
    public ResponseEntity<GetUserDto> registerUser(@Valid @RequestBody RegisterUserDto dto) {
        log.info("Register user: {}", dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.registerUser(dto));
    }

    @PostMapping("/auth")
    public ResponseEntity<GetUserJwtDto> authenticateUser(@Valid @RequestBody AuthUserDto dto) {
        log.info("Authenticate user: {}", dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authUserService.authenticateUser(dto));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<GetUserDto> getUserById(@PathVariable("userId") Long id) {
        log.info("Get user by id: {}", id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getUserById(id));
    }
}