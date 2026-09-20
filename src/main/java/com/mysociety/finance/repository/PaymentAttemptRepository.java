package com.mysociety.finance.repository;
import com.mysociety.finance.domain.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, UUID> { Optional<PaymentAttempt> findByIdAndSocietyId(UUID id, UUID societyId); Optional<PaymentAttempt> findBySocietyIdAndIdempotencyKey(UUID societyId, String idempotencyKey); }
