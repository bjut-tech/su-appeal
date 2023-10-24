package tech.bjut.su.appeal.entity;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.bjut.su.appeal.enums.CampusEnum;
import tech.bjut.su.appeal.jsonview.UserViews;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@JsonView(UserViews.Public.class)
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonView(UserViews.Private.class)
    private User user;

    @JsonView(UserViews.Private.class)
    private String contact;

    @Enumerated(EnumType.STRING)
    private CampusEnum campus;

    @Column(nullable = false, length = 65535)
    private String content;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable
    private List<Attachment> attachments;

    @OneToOne(fetch = FetchType.EAGER)
    private Answer answer;

    @Column(nullable = false)
    private boolean published = false;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
