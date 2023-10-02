package tech.bjut.su.appeal.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.bjut.su.appeal.enums.UserRoleEnum;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = 16)
    private String uid;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private UserRoleEnum role;

    private String name;

    private boolean admin = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.DETACH)
    private List<Question> questions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.DETACH)
    private List<Answer> answers;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
