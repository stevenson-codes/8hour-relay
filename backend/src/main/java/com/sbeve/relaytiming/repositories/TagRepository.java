package com.sbeve.relaytiming.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbeve.relaytiming.entities.TagEntity;

public interface TagRepository extends JpaRepository<TagEntity, String> {
}
