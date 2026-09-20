package com.mysociety.finance.service;

import com.mysociety.finance.domain.*;
import com.mysociety.finance.outbox.*;
import com.mysociety.finance.repository.*;
import com.mysociety.finance.security.TenantContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class FinanceService {
    private final ChargeHeadRepository chargeHeads; private final BillingRunRepository billingRuns; private final InvoiceRepository invoices;
    private final InvoiceAdjustmentRepository adjustments; private final PaymentAttemptRepository payments; private final PaymentEventRepository paymentEvents;
    private final PaymentAllocationRepository allocations; private final ReceiptRepository receipts; private final TransactionalOutbox outbox;
    public FinanceService(ChargeHeadRepository c, BillingRunRepository b, InvoiceRepository i, InvoiceAdjustmentRepository a, PaymentAttemptRepository p, PaymentEventRepository e, PaymentAllocationRepository al, ReceiptRepository r, TransactionalOutbox o) {chargeHeads=c;billingRuns=b;invoices=i;adjustments=a;payments=p;paymentEvents=e;allocations=al;receipts=r;outbox=o;}
    public ChargeHead createChargeHead(TenantContext t, String code, String name, BigDecimal amount) { return chargeHeads.save(ChargeHead.create(t.societyId(), code, name, amount)); }
    @Transactional(readOnly=true) public Page<ChargeHead> chargeHeads(TenantContext t, Pageable p) { return chargeHeads.findBySocietyId(t.societyId(), p); }
    public BillingRun createRun(TenantContext t, String number, String period, java.time.LocalDate start, java.time.LocalDate end, java.time.LocalDate due) { return billingRuns.save(BillingRun.create(t.societyId(),number,period,start,end,due,t.userId())); }
    public BillingRun publishRun(TenantContext t, UUID id) { BillingRun run=billingRuns.findByIdAndSocietyId(id,t.societyId()).orElseThrow(this::notFound); run.publish(t.userId()); emit("invoice.billing-run.published",t, "billing-run", id, run); return run; }
    @Transactional(readOnly=true) public Page<Invoice> invoices(TenantContext t, Pageable p) { return invoices.findBySocietyId(t.societyId(),p); }
    @Transactional(readOnly=true) public Invoice invoice(TenantContext t, UUID id) { return invoices.findByIdAndSocietyId(id,t.societyId()).orElseThrow(this::notFound); }
    public InvoiceAdjustment adjust(TenantContext t, UUID invoiceId, String number, String type, BigDecimal amount, String reason) { invoice(t,invoiceId); InvoiceAdjustment adjustment=adjustments.save(InvoiceAdjustment.create(t.societyId(),invoiceId,number,type,amount,reason,t.userId())); emit("invoice.adjustment.requested",t,"invoice",invoiceId,adjustment); return adjustment; }
    public PaymentAttempt initiate(TenantContext t, UUID unitId, String key, String provider, BigDecimal amount) {
        if (key == null || key.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Idempotency-Key is required");
        return payments.findBySocietyIdAndIdempotencyKey(t.societyId(),key).orElseGet(() -> {
            PaymentAttempt payment=payments.save(PaymentAttempt.create(t.societyId(),unitId,t.userId(),"PAY-"+UUID.randomUUID(),key,provider,amount));
            emit("payment.initiated",t,"payment-attempt",payment.getId(),payment); return payment;
        });
    }
    @Transactional(readOnly=true) public PaymentAttempt payment(TenantContext t, UUID id) { return payments.findByIdAndSocietyId(id,t.societyId()).orElseThrow(this::notFound); }
    public PaymentAllocation allocate(TenantContext t, UUID paymentId, UUID invoiceId, BigDecimal amount) { PaymentAttempt p=payment(t,paymentId); Invoice i=invoice(t,invoiceId); if (amount.compareTo(i.getOutstandingAmount())>0 || amount.compareTo(p.getRequestedAmount())>0) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Allocation exceeds available balance"); return allocations.save(PaymentAllocation.create(t.societyId(),paymentId,invoiceId,amount)); }
    public PaymentAttempt refund(TenantContext t, UUID id) { PaymentAttempt payment=payment(t,id); payment.requestRefund(); emit("payment.refund.requested",t,"payment-attempt",id,payment); return payment; }
    @Transactional(readOnly=true) public Receipt receipt(TenantContext t, UUID paymentId) { return receipts.findByPaymentAttemptIdAndSocietyId(paymentId,t.societyId()).orElseThrow(this::notFound); }
    public void acceptedWebhook(UUID societyId, UUID paymentId, String provider, String eventId, String type, String payload) { PaymentEvent event=PaymentEvent.received(societyId,paymentId,provider,eventId,type,true,payload); paymentEvents.save(event); event.processed(); }
    private ResponseStatusException notFound() { return new ResponseStatusException(HttpStatus.NOT_FOUND,"Finance resource not found"); }
    private void emit(String type,TenantContext t,String aggregate,UUID id,Object payload) { outbox.append(new DomainEvent(UUID.randomUUID(),type,1,Instant.now(),t.societyId(),aggregate,id,t.correlationId(),payload)); }
}
