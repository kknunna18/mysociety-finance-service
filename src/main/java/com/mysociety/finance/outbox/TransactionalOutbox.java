package com.mysociety.finance.outbox;

public interface TransactionalOutbox {
    void append(DomainEvent event);
}
