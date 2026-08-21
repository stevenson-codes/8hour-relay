package com.sbeve.relaytiming.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
public class TagEntity {
    @Id
    @Column(name = "epc_hex")
    private String epcHex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagType tagType;

    public TagEntity() {
    }

    public TagEntity(String epcHex, TagType tagType) {
        this.epcHex = epcHex;
        this.tagType = tagType;
    }

    public String getEpcHex() {
        return epcHex;
    }

    public TagType getTagType() {
        return tagType;
    }

    public void setTagType(TagType tagType) {
        this.tagType = tagType;
    }
}
