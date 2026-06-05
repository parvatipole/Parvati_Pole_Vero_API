package com.vero.api.service;

import com.vero.api.dto.TransactionRequest;
import com.vero.api.model.Category;
import com.vero.api.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TransactionService {

    /**
     * Returns every transaction in the system.
     */
    List<Transaction> getAllTransactions();

    /**
     * Returns the transaction with the given ID, or empty if it does not exist.
     */
    Optional<Transaction> getTransactionById(Long id);

    /**
     * Returns every transaction belonging to the given account.
     */
    List<Transaction> getTransactionsByAccount(Long accountId);

    /**
     * Returns every transaction whose transaction date falls between the given
     * start and end dates, inclusive on both ends.
     */
    List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Persists a new transaction built from the supplied request and returns the saved entity.
     */
    Transaction createTransaction(TransactionRequest request);

    /**
     * Deletes the transaction with the given ID.
     */
    void deleteTransaction(Long id);

    /**
     * Returns total spend grouped by category for the given calendar month.
     */
    Map<Category, BigDecimal> calculateMonthlySpend(int year, int month);

    /**
     * Returns the top N categories by total spend, in descending order, computed
     * over the supplied transactions.
     */
    Map<Category, BigDecimal> getTopSpendingCategories(List<Transaction> transactions, int topN);
}
