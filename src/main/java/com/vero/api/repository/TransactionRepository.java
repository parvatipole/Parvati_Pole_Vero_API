package com.vero.api.repository;

import com.vero.api.model.Category;
import com.vero.api.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Data access layer for Transaction entities. Extends Spring Data JPA to provide
 * standard CRUD operations and custom query methods.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountId(Long accountId);

    List<Transaction> findByCategory(Category category);
}
