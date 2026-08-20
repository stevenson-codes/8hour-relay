package com.org.relaytiming.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "teams")
public class TeamEntity {
    
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String epcHex;

    @Column(nullable = false, unique = true)
    private String name;

    protected TeamEntity() {}

    public TeamEntity(Long id, String epcHex, String name) {
        this.id = id;
        this.epcHex = epcHex;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEpcHex() {
        return epcHex;
    }

    public void setEpcHex(String epcHex) {
        this.epcHex = epcHex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
