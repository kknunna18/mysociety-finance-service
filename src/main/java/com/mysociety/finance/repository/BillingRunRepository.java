package com.mysociety.finance.repository;

import com.mysociety.finance.domain.BillingRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface BillingRunRepository extends JpaRepository<BillingRun, UUID> {
    Optional<BillingRun> findByIdAndSocietyId(UUID id, UUID societyId);
}
