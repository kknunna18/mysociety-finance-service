package com.mysociety.finance.repository;

import com.mysociety.finance.domain.ChargeHead;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface ChargeHeadRepository extends JpaRepository<ChargeHead, UUID> {
    Page<ChargeHead> findBySocietyId(UUID societyId, Pageable pageable);

    Optional<ChargeHead> findByIdAndSocietyId(UUID id, UUID societyId);
}
