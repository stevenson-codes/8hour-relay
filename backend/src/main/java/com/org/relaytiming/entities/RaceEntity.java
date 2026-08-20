package com.org.relaytiming.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "races")
public class RaceEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    protected RaceEntity() {
    }

    public RaceEntity(Long id, LocalDateTime startedAt) {
        this.id = id;
        this.startedAt = startedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
}
