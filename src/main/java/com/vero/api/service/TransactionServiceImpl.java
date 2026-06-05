package com.vero.api.service;

import com.vero.api.dto.TransactionRequest;
import com.vero.api.model.Category;
import com.vero.api.model.Transaction;
import com.vero.api.repository.TransactionRepository;
import com.vero.api.util.BudgetCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;

    @Override
    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

    @Override
    public Optional<Transaction> getTransactionById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Transaction> getTransactionsByAccount(Long accountId) {
        return repository.findByAccountId(accountId);
    }

    @Override
    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        // TODO: implement date range filtering
        return Collections.emptyList();
    }

    @Override
    public Transaction createTransaction(TransactionRequest request) {
        Transaction transaction = Transaction.builder()
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .category(request.getCategory())
                .transactionDate(request.getTransactionDate())
                .build();
        return repository.save(transaction);
    }

    @Override
    public void deleteTransaction(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Map<Category, BigDecimal> calculateMonthlySpend(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        return repository.findAll().stream()
                .filter(t -> t.getTransactionDate().isAfter(startOfMonth)
                        && !t.getTransactionDate().isAfter(endOfMonth))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
    }

    @Override
    public Map<Category, BigDecimal> getTopSpendingCategories(List<Transaction> transactions, int topN) {
        return BudgetCalculator.getTopSpendingCategories(transactions, topN);
    }

    public List<Transaction> getCategoryTransactionsForMonth(Category category, int year, int month) {
        return repository.findByCategoryAndMonth(category, year, month);
    }
}
