package git.xlamid.eventmanagerservice.location.entity;

import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "locations")
public class LocationEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = 300)
    private String name;

    @Column(name = "address", nullable = false, length = 1000)
    private String address;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    private List<EventEntity> events;
}