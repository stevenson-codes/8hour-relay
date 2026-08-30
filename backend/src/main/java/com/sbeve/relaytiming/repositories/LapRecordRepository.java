package com.sbeve.relaytiming.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbeve.relaytiming.entities.LapRecordEntity;
import com.sbeve.relaytiming.entities.LapStatus;
import com.sbeve.relaytiming.entities.TagEntity;

public interface LapRecordRepository extends JpaRepository<LapRecordEntity, Long> {
    Optional<LapRecordEntity> findTopByTagEpcHexAndStatusInOrderByTimestampDesc(String epcHex, Collection<LapStatus> statuses);
    Optional<LapRecordEntity> findTopByTagEpcHexOrderByTimestampDesc(String epcHex);
    Optional<LapRecordEntity> findTopByTagAndStatusOrderByTimestampDesc(TagEntity tag, LapStatus status);
    List<LapRecordEntity> findByTagAndStatus(TagEntity tag, LapStatus status);
}
