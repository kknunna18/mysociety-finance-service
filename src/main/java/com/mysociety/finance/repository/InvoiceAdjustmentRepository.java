package com.mysociety.finance.repository;
import com.mysociety.finance.domain.InvoiceAdjustment; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface InvoiceAdjustmentRepository extends JpaRepository<InvoiceAdjustment, UUID> {}
