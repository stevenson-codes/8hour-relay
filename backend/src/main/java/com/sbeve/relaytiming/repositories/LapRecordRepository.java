package com.sbeve.relaytiming.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbeve.relaytiming.entities.LapRecordEntity;

public interface LapRecordRepository extends JpaRepository<LapRecordEntity, Long> {
}
