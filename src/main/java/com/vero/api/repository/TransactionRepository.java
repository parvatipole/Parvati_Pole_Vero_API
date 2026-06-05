package com.vero.api.repository;

import com.vero.api.model.Category;
import com.vero.api.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Data access layer for Transaction entities. Extends Spring Data JPA to provide
 * standard CRUD operations and custom query methods.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountId(Long accountId);

    List<Transaction> findByCategory(Category category);

    /**
     * FIX (Bug 2 + Bug 4): Added missing method that TransactionServiceImpl.getCategoryTransactionsForMonth()
     * was calling. Spring Data cannot derive a query named findByCategoryAndMonth from the
     * method name alone (month is not a direct field), so an explicit JPQL @Query is required.
     * Also added the necessary @Query and @Param imports (Bug 4).
     */
    @Query("SELECT t FROM Transaction t " +
           "WHERE t.category = :category " +
           "AND YEAR(t.transactionDate) = :year " +
           "AND MONTH(t.transactionDate) = :month")
    List<Transaction> findByCategoryAndMonth(
            @Param("category") Category category,
            @Param("year") int year,
            @Param("month") int month
    );
}