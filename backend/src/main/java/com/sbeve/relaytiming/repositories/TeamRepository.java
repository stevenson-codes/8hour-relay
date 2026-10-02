package com.sbeve.relaytiming.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbeve.relaytiming.entities.TagEntity;
import com.sbeve.relaytiming.entities.TeamEntity;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {
    boolean existsByTag(TagEntity tag);
}
