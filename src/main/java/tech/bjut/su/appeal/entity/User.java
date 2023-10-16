package tech.bjut.su.appeal.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.bjut.su.appeal.enums.UserRoleEnum;
import tech.bjut.su.appeal.jsonview.UserViews;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@JsonView(UserViews.Public.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = 16)
    private String uid;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @JsonIgnore
    private UserRoleEnum role;

    private String name;

    @JsonIgnore
    private boolean admin = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.DETACH)
    @JsonIgnore
    private List<Question> questions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.DETACH)
    @JsonIgnore
    private List<Answer> answers;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
