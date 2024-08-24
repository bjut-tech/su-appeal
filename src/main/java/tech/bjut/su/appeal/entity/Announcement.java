package tech.bjut.su.appeal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.bjut.su.appeal.jsonview.UserViews;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@JsonView(UserViews.Public.class)
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonView(UserViews.Admin.class)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    private AnnouncementCategory category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 65535)
    private String content;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable
    private List<Attachment> attachments;

    private boolean pinned = false;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
