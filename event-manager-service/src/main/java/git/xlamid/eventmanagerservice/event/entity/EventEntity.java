package git.xlamid.eventmanagerservice.event.entity;

import git.xlamid.eventmanagerservice.location.entity.LocationEntity;
import git.xlamid.eventmanagerservice.registration.entity.RegistrationEntity;
import git.xlamid.eventmanagerservice.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
public class EventEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime date;

    @Column(name = "cost")
    private Integer cost;

    @Column(name = "duration_minutes")
    private Integer duration;

    @Column(name = "max_places")
    private Integer maxPlaces;

    @Column(name = "occupied_places")
    private Integer occupiedPlaces = 0;

    @Column(name = "status", nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private LocationEntity location;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity owner;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    private List<RegistrationEntity> registrations = new ArrayList<>();
}