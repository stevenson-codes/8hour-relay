package com.org.relaytiming.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "runners")
public class RunnerEntity {
    @Id
    @Column(nullable = false, unique = true)
    private Long id;

    @Column(nullable = false, unique = true)
    private String runnerID;
    
    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    public RunnerEntity() {
    }

    public RunnerEntity(TeamEntity team, String runnerID, String name) {
        this.team = team;
        this.runnerID = runnerID;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TeamEntity getTeam() {
        return team;
    }

    public void setTeam(TeamEntity team) {
        this.team = team;
    }

    public String getRunnerID() {
        return runnerID;
    }

    public void setRunnerID(String runnerID) {
        this.runnerID = runnerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
