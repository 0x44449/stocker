package app.sandori.stocker.api.entities;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "allowed_user")
public class AllowedUserEntity {

    @Id
    @Column(name = "uid")
    private String uid;

    @Column(name = "memo")
    private String memo;

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    public AllowedUserEntity() {}

    public String getUid() {
        return uid;
    }

    public String getMemo() {
        return memo;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
