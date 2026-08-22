package com.sbeve.relaytiming.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "lap_records")
public class LapRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "epc_hex", referencedColumnName = "epc_hex", nullable = false)
    private TagEntity tag;

    @Column(nullable = false)
    private Instant timestamp;
    
    @Column(nullable = true)
    private Long lapTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LapStatus status;

    public LapRecordEntity() {
    }

    public LapRecordEntity(TagEntity tag, Instant timestamp, Long lapTime, LapStatus status) {
        this.tag = tag;
        this.timestamp = timestamp;
        this.lapTime = lapTime;
        this.status = status;
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

    public Long getLapTime() {
        return lapTime;
    }

    public void setLapTime(Long lapTime) {
        this.lapTime = lapTime;
    }

    public LapStatus getStatus() {
        return status;
    }

    public void setStatus(LapStatus status) {
        this.status = status;
    }
}
