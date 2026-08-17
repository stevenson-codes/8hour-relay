package com.org.relaytiming.repositories;

import com.org.relaytiming.entities.LapRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LapRecordRepository extends JpaRepository<LapRecordEntity, Long> {
}
