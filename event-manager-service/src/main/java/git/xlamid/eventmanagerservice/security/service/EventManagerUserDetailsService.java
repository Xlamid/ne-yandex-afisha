package git.xlamid.eventmanagerservice.security.service;

import git.xlamid.eventmanagerservice.exception.model.notfound.UserNotFoundException;
import git.xlamid.eventmanagerservice.security.model.EventManagerUserDetails;
import git.xlamid.eventmanagerservice.user.entity.UserEntity;
import git.xlamid.eventmanagerservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventManagerUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByLogin(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with login: " + username));
        return new EventManagerUserDetails(
                userEntity.getId(),
                userEntity.getAge(),
                userEntity.getLogin(),
                userEntity.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(userEntity.getRole()))
        );
    }
}