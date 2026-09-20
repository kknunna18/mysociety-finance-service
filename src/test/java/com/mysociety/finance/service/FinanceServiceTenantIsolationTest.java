package com.mysociety.finance.service;

import com.mysociety.finance.outbox.TransactionalOutbox;
import com.mysociety.finance.repository.*;
import com.mysociety.finance.security.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FinanceServiceTenantIsolationTest {
    @Test void paymentLookupAlwaysUsesJwtSocietyAndRejectsAnotherTenant() {
        PaymentAttemptRepository payments=mock(PaymentAttemptRepository.class);
        UUID paymentId=UUID.randomUUID(); UUID jwtSociety=UUID.randomUUID();
        when(payments.findByIdAndSocietyId(paymentId,jwtSociety)).thenReturn(Optional.empty());
        FinanceService service=new FinanceService(mock(ChargeHeadRepository.class),mock(BillingRunRepository.class),mock(InvoiceRepository.class),mock(InvoiceAdjustmentRepository.class),payments,mock(PaymentEventRepository.class),mock(PaymentAllocationRepository.class),mock(ReceiptRepository.class),mock(TransactionalOutbox.class));
        assertThatThrownBy(() -> service.payment(new TenantContext(jwtSociety,UUID.randomUUID(),"cid"),paymentId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode.value").isEqualTo(404);
        verify(payments).findByIdAndSocietyId(paymentId,jwtSociety);
        verify(payments,never()).findById(paymentId);
    }
}
