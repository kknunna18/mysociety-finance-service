package com.mysociety.finance.repository;
import com.mysociety.finance.domain.Invoice;
import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> { Page<Invoice> findBySocietyId(UUID societyId, Pageable pageable); Optional<Invoice> findByIdAndSocietyId(UUID id, UUID societyId); }
