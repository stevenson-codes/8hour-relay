package com.org.relaytiming.repositories;

import com.org.relaytiming.entities.LapRecords;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LapRecordRepository extends JpaRepository<LapRecords, Long> {
}
