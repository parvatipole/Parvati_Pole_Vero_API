package com.vero.api.dto;

import com.vero.api.model.Category;
import com.vero.api.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Outbound response payload representing a single transaction.
 *
 * This DTO is the contract between the API and its consumers. It must never be
 * replaced by the raw {@link Transaction} entity: returning an entity directly
 * would expose JPA internals (lazy-load proxies, audit timestamps, Hibernate
 * metadata), couple the API shape to the persistence model, and make it easy to
 * accidentally leak fields that should stay server-side.
 *
 * Fields added here must be a deliberate decision — not whatever the entity
 * happens to contain.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;
    private Long accountId;
    private BigDecimal amount;
    private String description;
    private Category category;
    private LocalDate transactionDate;

    /**
     * Constructs a {@code TransactionResponse} from a persisted {@link Transaction}.
     * Only the fields the API intends to expose are mapped here.
     */
    public static TransactionResponse fromTransaction(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .accountId(t.getAccountId())
                .amount(t.getAmount())
                .description(t.getDescription())
                .category(t.getCategory())
                .transactionDate(t.getTransactionDate())
                .build();
    }
}