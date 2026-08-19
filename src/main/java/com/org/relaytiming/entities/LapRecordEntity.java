package com.org.relaytiming.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "lap_records")
public class LapRecordEntity {
    
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_epc_hex", referencedColumnName = "epcHex", nullable = true)
    private TeamEntity team;

    @ManyToOne
    @JoinColumn(name = "runner_epc_hex", referencedColumnName = "epcHex", nullable = true)
    private RunnerEntity runner;

    @Column(nullable = false)
    private Integer lapNumber;

    @Column(nullable = true)
    private Double lapTime;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public LapRecordEntity() {
    }

    public LapRecordEntity(RunnerEntity runner, Integer lapNumber, Double lapTime, LocalDateTime timestamp) {
        this.team = null;
        this.runner = runner;
        this.lapNumber = lapNumber;
        this.lapTime = lapTime;
        this.timestamp = timestamp;
    }

    public LapRecordEntity(TeamEntity team, Integer lapNumber, Double lapTime, LocalDateTime timestamp) {
        this.team = team;
        this.runner = null;
        this.lapNumber = lapNumber;
        this.lapTime = lapTime;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RunnerEntity getRunner() {
        return runner;
    }

    public void setRunner(RunnerEntity runner) {
        this.runner = runner;
    }

    public TeamEntity getTeam() {
        return team;
    }

    public void setTeam(TeamEntity team) {
        this.team = team;
    }

    public Integer getLapNumber() {
        return lapNumber;
    }

    public void setLapNumber(Integer lapNumber) {
        this.lapNumber = lapNumber;
    }

    public Double getLapTime() {
        return lapTime;
    }

    public void setLapTime(Double lapTime) {
        this.lapTime = lapTime;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
