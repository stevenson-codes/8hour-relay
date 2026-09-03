package com.sbeve.relaytiming.entities;

import com.sbeve.relaytiming.entities.enums.RunnerStatus;
import com.sbeve.relaytiming.entities.enums.Sex;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "runners")
public class RunnerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RunnerStatus status;

    @Column(nullable = false)
    private Integer leg;

    @Column(name = "bib")
    private String bib;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex")
    private Sex sex;

    @OneToOne
    @JoinColumn(name = "epc_hex", nullable = false)
    private TagEntity tag;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    public RunnerEntity() {
    }

    public RunnerEntity(String name, RunnerStatus status, Integer leg, TagEntity tag, TeamEntity team) {
        this.name = name;
        this.status = status;
        this.leg = leg;
        this.tag = tag;
        this.team = team;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RunnerStatus getStatus() {
        return status;
    }

    public void setStatus(RunnerStatus status) {
        this.status = status;
    }

    public Integer getLeg() {
        return leg;
    }

    public void setLeg(Integer leg) {
        this.leg = leg;
    }

    public String getBib() {
        return bib;
    }

    public void setBib(String bib) {
        this.bib = bib;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public TagEntity getTag() {
        return tag;
    }

    public void setTag(TagEntity tag) {
        this.tag = tag;
    }

    public TeamEntity getTeam() {
        return team;
    }

    public void setTeam(TeamEntity team) {
        this.team = team;
    }
}
