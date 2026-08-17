package com.org.relaytiming.repositories;

import com.org.relaytiming.entities.Teams;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Teams, Long> {
}
