package Model.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kinesitherapeutes")
@Builder
public class Kinesitherapeute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, unique = true, nullable = false)
    private UUID id ;

    //------- User Relation--------
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    //-----------------------------

    //------- Kinesitherapeute Details--------
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "speciality", nullable = false, length = 100)
    private String speciality;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //--------------------------------------


    //created_at automatique => Évite de remplir la date manuellement "AUTO TIMESTAMP".
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    //------- Patient Relation--------
    @OneToMany(mappedBy = "kine", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Patient> patients;
    //-----------------------------

}
