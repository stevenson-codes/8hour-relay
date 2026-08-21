package com.sbeve.relaytiming.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class LapRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "epc_hex", referencedColumnName = "epc_hex", nullable = false)
    private TagEntity tag;

    @Column(nullable = false)
    private Instant timestamp;

    public LapRecordEntity() {
    }

    public LapRecordEntity(TagEntity tag, Instant timestamp) {
        this.tag = tag;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public TagEntity getTag() {
        return tag;
    }

    public void setTag(TagEntity tag) {
        this.tag = tag;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
