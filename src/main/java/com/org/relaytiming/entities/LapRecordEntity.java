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
    @JoinColumn(name = "runner_id", nullable = false)
    private RunnerEntity runner;

    @Column(nullable = false)
    private Integer teamLapNumber;

    @Column(nullable = false)
    private Integer runnerLapNumber;

    @Column(nullable = false)
    private Double lapTime;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public LapRecordEntity() {
    }

    public LapRecordEntity(RunnerEntity runner, Integer teamLapNumber, Integer runnerLapNumber, Double lapTime, LocalDateTime timestamp) {
        this.runner = runner;
        this.teamLapNumber = teamLapNumber;
        this.runnerLapNumber = runnerLapNumber;
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

    public Integer getTeamLapNumber() {
        return teamLapNumber;
    }

    public void setTeamLapNumber(Integer teamLapNumber) {
        this.teamLapNumber = teamLapNumber;
    }

    public Integer getRunnerLapNumber() {
        return runnerLapNumber;
    }

    public void setRunnerLapNumber(Integer runnerLapNumber) {
        this.runnerLapNumber = runnerLapNumber;
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
