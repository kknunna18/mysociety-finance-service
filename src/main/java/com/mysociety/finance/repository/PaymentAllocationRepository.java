package com.mysociety.finance.repository;
import com.mysociety.finance.domain.PaymentAllocation; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, UUID> {}
