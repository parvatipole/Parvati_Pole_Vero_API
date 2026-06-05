package com.vero.api.controller;

import com.vero.api.dto.TransactionRequest;
import com.vero.api.dto.TransactionResponse;
import com.vero.api.model.Category;
import com.vero.api.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST controller for transaction management.
 *
 * This layer is responsible solely for HTTP: request parsing, response shaping,
 * and status codes. All business logic lives in {@link TransactionService}.
 *
 * Every write endpoint returns a {@link TransactionResponse} DTO, never the raw
 * {@link com.vero.api.model.Transaction} entity. Returning an entity directly
 * would couple the API contract to the persistence model and could leak
 * server-internal fields (e.g. audit timestamps, Hibernate proxies).
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        List<TransactionResponse> body = service.getAllTransactions().stream()
                .map(TransactionResponse::fromTransaction)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
        return service.getTransactionById(id)
                .map(TransactionResponse::fromTransaction)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccount(
            @PathVariable Long accountId) {
        List<TransactionResponse> body = service.getTransactionsByAccount(accountId).stream()
                .map(TransactionResponse::fromTransaction)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/monthly-spend")
    public ResponseEntity<Map<Category, BigDecimal>> getMonthlySpend(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(service.calculateMonthlySpend(year, month));
    }

    /**
     * Creates a new transaction and returns a 201 Created response.
     *
     * Bug fix: the original method returned {@code ResponseEntity<Transaction>} —
     * a raw entity. Changed to {@code ResponseEntity<TransactionResponse>} so that
     * the response is shaped by the DTO contract, not the persistence model.
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request) {
        TransactionResponse body = TransactionResponse.fromTransaction(
                service.createTransaction(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /**
     * Deletes a transaction by ID and returns 204 No Content.
     *
     * Returns 404 Not Found if no transaction exists with the given ID.
     * Previously this endpoint returned 204 unconditionally — a delete against a
     * non-existent ID was a silent no-op from the caller's perspective.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        try {
            service.deleteTransaction(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}