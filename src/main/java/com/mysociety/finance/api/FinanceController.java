package com.mysociety.finance.api;

import com.mysociety.finance.config.CorrelationIdFilter;
import com.mysociety.finance.domain.*;
import com.mysociety.finance.security.*;
import com.mysociety.finance.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping
public class FinanceController {
    private final FinanceService service; private final TenantContextResolver tenant;
    public FinanceController(FinanceService service, TenantContextResolver tenant) {this.service=service;this.tenant=tenant;}
    @Operation(summary="Create a charge head") @PostMapping("/charge-heads")
    @ResponseStatus(HttpStatus.CREATED) public ChargeHead createChargeHead(@Valid @RequestBody ChargeHeadRequest request, Authentication auth, @RequestHeader(value=CorrelationIdFilter.HEADER, required=false) String correlation) { return service.createChargeHead(context(auth,correlation),request.code(),request.name(),request.defaultAmount()); }
    @GetMapping("/charge-heads") public Page<ChargeHead> chargeHeads(Authentication auth, @PageableDefault(size=25,sort="code") Pageable page, @RequestHeader(value=CorrelationIdFilter.HEADER,required=false) String correlation) { return service.chargeHeads(context(auth,correlation),page); }
    @PostMapping("/billing-runs") @ResponseStatus(HttpStatus.CREATED) public BillingRun createBillingRun(@Valid @RequestBody BillingRunRequest request, Authentication auth, @RequestHeader(value=CorrelationIdFilter.HEADER,required=false) String correlation) { return service.createRun(context(auth,correlation),request.runNumber(),request.billingPeriod(),request.periodStart(),request.periodEnd(),request.dueDate()); }
    @PostMapping("/billing-runs/{id}/publish") public BillingRun publishBillingRun(@PathVariable UUID id, Authentication auth, @RequestHeader(value=CorrelationIdFilter.HEADER,required=false) String correlation) { return service.publishRun(context(auth,correlation),id); }
    @GetMapping("/invoices") public Page<Invoice> invoices(Authentication auth, @PageableDefault(size=25,sort="dueDate") Pageable page, @RequestHeader(value=CorrelationIdFilter.HEADER,required=false) String correlation) { return service.invoices(context(auth,correlation),page); }
    @GetMapping("/invoices/{id}") public Invoice invoice(@PathVariable UUID id, Authentication auth, @RequestHeader(value=CorrelationIdFilter.HEADER,required=false) String correlation) { return service.invoice(context(auth,correlation),id); }
    @PostMapping("/invoices/{id}/adjustments") @ResponseStatus(HttpStatus.CREATED) public InvoiceAdjustment adjust(@PathVariable UUID id,@Valid @RequestBody AdjustmentRequest request,Authentication auth,@RequestHeader(value=CorrelationIdFilter.HEADER,required=false) String correlation) { return service.adjust(context(auth,correlation),id,request.adjustmentNumber(),request.adjustmentType(),request.amount(),request.reason()); }
    @PostMapping("/payments") @ResponseStatus(HttpStatus.CREATED) public PaymentAttempt initiate(@RequestHeader("Idempotency-Key") @NotBlank @Size(max=150) String key,@Valid @RequestBody PaymentRequest request,Authentication auth,@RequestHeader(value=CorrelationIdFilter.HEADER,required=false) String correlation) { return service.initiate(context(auth,correlation),request.unitId(),key,request.provider(),request.amount()); }
    @GetMapping("/payments/{id}") public PaymentAttempt payment(@PathVariable UUID id,Authentication auth,@RequestHeader(value=CorrelationIdFilter.HEADER,required=false) String correlation) { return service.payment(context(auth,correlation),id); }
    @PostMapping("/payments/{id}/allocations") @ResponseStatus(HttpStatus.CREATED) public PaymentAllocation allocate(@PathVariable UUID id,@Valid @RequestBody AllocationRequest request,Authentication auth,@RequestHeader(value=CorrelationIdFilter.HEADER,required=false) String correlation) { return service.allocate(context(auth,correlation),id,request.invoiceId(),request.amount()); }
    @PostMapping("/payments/{id}/refunds") public PaymentAttempt refund(@PathVariable UUID id,Authentication auth,@RequestHeader(value=CorrelationIdFilter.HEADER,required=false) String correlation) { return service.refund(context(auth,correlation),id); }
    @GetMapping("/payments/{id}/receipt") public Receipt receipt(@PathVariable UUID id,Authentication auth,@RequestHeader(value=CorrelationIdFilter.HEADER,required=false) String correlation) { return service.receipt(context(auth,correlation),id); }
    @GetMapping("/reconciliation") public ResponseEntity<Void> reconciliation(Authentication auth,@RequestHeader(value=CorrelationIdFilter.HEADER,required=false) String correlation) { context(auth,correlation); return ResponseEntity.accepted().build(); }
    private TenantContext context(Authentication auth,String correlation) { return tenant.current(auth,correlation == null ? "generated-at-filter" : correlation); }
    public record ChargeHeadRequest(@NotBlank @Size(max=40) String code,@NotBlank @Size(max=120) String name,@DecimalMin("0.00") BigDecimal defaultAmount) {}
    public record BillingRunRequest(@NotBlank @Size(max=60) String runNumber,@NotBlank @Size(max=20) String billingPeriod,@NotNull LocalDate periodStart,@NotNull LocalDate periodEnd,@NotNull LocalDate dueDate) {}
    public record AdjustmentRequest(@NotBlank @Size(max=80) String adjustmentNumber,@Pattern(regexp="DEBIT|CREDIT|WAIVER") String adjustmentType,@NotNull @DecimalMin(value="0.01") BigDecimal amount,@NotBlank @Size(max=1000) String reason) {}
    public record PaymentRequest(@NotNull UUID unitId,@NotBlank @Size(max=30) String provider,@NotNull @DecimalMin(value="0.01") BigDecimal amount) {}
    public record AllocationRequest(@NotNull UUID invoiceId,@NotNull @DecimalMin(value="0.01") BigDecimal amount) {}
}
