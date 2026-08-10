package git.xlamid.eventmanagerservice.user.entity;

import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import git.xlamid.eventmanagerservice.registration.entity.RegistrationEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "login", unique = true, nullable = false, length = 300)
    private String login;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "password_hash", nullable = false, length = 256)
    private String passwordHash;

    @Column(name = "role")
    private String role;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<EventEntity> events = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<RegistrationEntity> registrations = new ArrayList<>();
}