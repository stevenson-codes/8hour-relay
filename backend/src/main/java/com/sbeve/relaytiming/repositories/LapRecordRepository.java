package com.sbeve.relaytiming.repositories;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbeve.relaytiming.entities.LapRecordEntity;
import com.sbeve.relaytiming.entities.LapStatus;

public interface LapRecordRepository extends JpaRepository<LapRecordEntity, Long> {
    Optional<LapRecordEntity> findTopByTagEpcHexAndStatusInOrderByTimestampDesc(String epcHex, Collection<LapStatus> statuses);
}
