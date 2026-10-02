package com.mysociety.finance.repository;

import com.mysociety.finance.domain.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface ReceiptRepository extends JpaRepository<Receipt, UUID> {
    Optional<Receipt> findByPaymentAttemptIdAndSocietyId(UUID paymentAttemptId, UUID societyId);
}
