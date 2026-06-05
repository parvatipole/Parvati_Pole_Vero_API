package com.vero.api;

import com.vero.api.model.Category;
import com.vero.api.model.Transaction;
import com.vero.api.repository.TransactionRepository;
import com.vero.api.service.TransactionServiceImpl;
import com.vero.api.dto.TransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Candidate-authored tests covering fixed bugs and edge cases not addressed
 * in TransactionServiceTest.java.
 */
@ExtendWith(MockitoExtension.class)
class TransactionCandidateTest {

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private TransactionServiceImpl service;

    private List<Transaction> sampleTransactions;

    @BeforeEach
    void setUp() {
        // Dec 1 — boundary date; the off-by-one bug excluded this
        Transaction dec1Food = Transaction.builder()
                .id(1L).accountId(1L)
                .amount(new BigDecimal("87.45"))
                .description("Whole Foods Market")
                .category(Category.FOOD)
                .transactionDate(LocalDate.of(2024, 12, 1))
                .build();

        // Dec 31 — last day of month boundary
        Transaction dec31Transport = Transaction.builder()
                .id(2L).accountId(1L)
                .amount(new BigDecimal("15.00"))
                .description("Last day cab")
                .category(Category.TRANSPORT)
                .transactionDate(LocalDate.of(2024, 12, 31))
                .build();

        // Mid-month
        Transaction dec14Food = Transaction.builder()
                .id(3L).accountId(1L)
                .amount(new BigDecimal("56.78"))
                .description("Trader Joes")
                .category(Category.FOOD)
                .transactionDate(LocalDate.of(2024, 12, 14))
                .build();

        // Different month — must be excluded
        Transaction nov5Food = Transaction.builder()
                .id(4L).accountId(1L)
                .amount(new BigDecimal("73.00"))
                .description("November groceries")
                .category(Category.FOOD)
                .transactionDate(LocalDate.of(2024, 11, 5))
                .build();

        // Different year — must be excluded
        Transaction jan2023Food = Transaction.builder()
                .id(5L).accountId(1L)
                .amount(new BigDecimal("100.00"))
                .description("Last year groceries")
                .category(Category.FOOD)
                .transactionDate(LocalDate.of(2023, 12, 1))
                .build();

        sampleTransactions = List.of(dec1Food, dec31Transport, dec14Food, nov5Food, jan2023Food);
    }

    // -------------------------------------------------------------------------
    // Bug 1 regression — calculateMonthlySpend boundary inclusivity
    // -------------------------------------------------------------------------

    @Test
    void calculateMonthlySpend_includesTransactionOnFirstDayOfMonth() {
        // Regression for Bug 1: .isAfter(startOfMonth) excluded the 1st.
        // Dec 1 (87.45) + Dec 14 (56.78) = 144.23 — if 87.45 is missing the fix is broken.
        when(repository.findAll()).thenReturn(sampleTransactions);

        Map<Category, BigDecimal> result = service.calculateMonthlySpend(2024, 12);

        assertThat(result.get(Category.FOOD)).isEqualByComparingTo("144.23");
    }

    @Test
    void calculateMonthlySpend_includesTransactionOnLastDayOfMonth() {
        // Ensures the end boundary is also inclusive (Dec 31 transport must appear).
        when(repository.findAll()).thenReturn(sampleTransactions);

        Map<Category, BigDecimal> result = service.calculateMonthlySpend(2024, 12);

        assertThat(result.get(Category.TRANSPORT)).isEqualByComparingTo("15.00");
    }

    @Test
    void calculateMonthlySpend_excludesPreviousMonth() {
        // November transactions must not bleed into December totals.
        when(repository.findAll()).thenReturn(sampleTransactions);

        Map<Category, BigDecimal> decResult = service.calculateMonthlySpend(2024, 12);

        // FOOD in Dec = 144.23, not 144.23 + 73.00 (Nov)
        assertThat(decResult.get(Category.FOOD)).isEqualByComparingTo("144.23");
    }

    @Test
    void calculateMonthlySpend_excludesSameMonthDifferentYear() {
        // Dec 2023 transaction must not appear in Dec 2024 report.
        when(repository.findAll()).thenReturn(sampleTransactions);

        Map<Category, BigDecimal> result = service.calculateMonthlySpend(2024, 12);

        // 2023 transaction was 100.00 FOOD; if included, total would be 244.23
        assertThat(result.get(Category.FOOD)).isEqualByComparingTo("144.23");
    }

    @Test
    void calculateMonthlySpend_emptyRepository_returnsEmptyMap() {
        // Edge case: no transactions at all should return an empty map, not throw.
        when(repository.findAll()).thenReturn(Collections.emptyList());

        Map<Category, BigDecimal> result = service.calculateMonthlySpend(2024, 12);

        assertThat(result).isEmpty();
    }

    @Test
    void calculateMonthlySpend_monthWithNoTransactions_returnsEmptyMap() {
        // Querying a month that has no transactions (e.g. January) returns empty map.
        when(repository.findAll()).thenReturn(sampleTransactions);

        Map<Category, BigDecimal> result = service.calculateMonthlySpend(2024, 1);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Bug 3 regression — getTransactionsByDateRange no longer returns empty list
    // -------------------------------------------------------------------------

    @Test
    void getTransactionsByDateRange_returnsTransactionsWithinRange() {
        // Regression for Bug 3: the stub always returned emptyList().
        when(repository.findAll()).thenReturn(sampleTransactions);

        List<Transaction> result = service.getTransactionsByDateRange(
                LocalDate.of(2024, 12, 1),
                LocalDate.of(2024, 12, 31)
        );

        // Dec 1, Dec 14, Dec 31 = 3 transactions; Nov and 2023 excluded
        assertThat(result).hasSize(3);
    }

    @Test
    void getTransactionsByDateRange_startDateIsInclusive() {
        when(repository.findAll()).thenReturn(sampleTransactions);

        List<Transaction> result = service.getTransactionsByDateRange(
                LocalDate.of(2024, 12, 1),
                LocalDate.of(2024, 12, 1)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTransactionDate()).isEqualTo(LocalDate.of(2024, 12, 1));
    }

    @Test
    void getTransactionsByDateRange_endDateIsInclusive() {
        when(repository.findAll()).thenReturn(sampleTransactions);

        List<Transaction> result = service.getTransactionsByDateRange(
                LocalDate.of(2024, 12, 31),
                LocalDate.of(2024, 12, 31)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTransactionDate()).isEqualTo(LocalDate.of(2024, 12, 31));
    }

    @Test
    void getTransactionsByDateRange_noMatchingTransactions_returnsEmpty() {
        when(repository.findAll()).thenReturn(sampleTransactions);

        List<Transaction> result = service.getTransactionsByDateRange(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31)
        );

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Bug 5 — BudgetCalculator.getTopSpendingCategories behaviour
    // -------------------------------------------------------------------------

    @Test
    void getTopSpendingCategories_limitsToTopN() {
        // Must never return more entries than topN, regardless of how many categories exist.
        Map<Category, BigDecimal> result = service.getTopSpendingCategories(sampleTransactions, 2);

        assertThat(result).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    void getTopSpendingCategories_emptyList_returnsEmptyMap() {
        // Edge case: passing an empty transaction list must not throw.
        Map<Category, BigDecimal> result = service.getTopSpendingCategories(Collections.emptyList(), 3);

        assertThat(result).isEmpty();
    }

    @Test
    void getTopSpendingCategories_topNExceedsCategoryCount_returnsAll() {
        // Asking for top-10 when only 2 categories exist should return 2, not throw.
        Map<Category, BigDecimal> result = service.getTopSpendingCategories(sampleTransactions, 10);

        // sampleTransactions has FOOD and TRANSPORT only
        assertThat(result).hasSizeLessThanOrEqualTo(2);
        assertThat(result).containsKey(Category.FOOD);
    }

    // -------------------------------------------------------------------------
    // General service behaviour
    // -------------------------------------------------------------------------

    @Test
    void deleteTransaction_delegatesToRepository() {
        service.deleteTransaction(1L);

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void getTransactionById_notFound_returnsEmpty() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThat(service.getTransactionById(999L)).isEmpty();
    }

    @Test
    void createTransaction_savesAndReturnsEntity() {
        TransactionRequest req = TransactionRequest.builder()
                .accountId(1L)
                .amount(new BigDecimal("9.99"))
                .description("Netflix")
                .category(Category.ENTERTAINMENT)
                .transactionDate(LocalDate.of(2024, 12, 15))
                .build();

        Transaction saved = Transaction.builder()
                .id(99L)
                .accountId(1L)
                .amount(new BigDecimal("9.99"))
                .description("Netflix")
                .category(Category.ENTERTAINMENT)
                .transactionDate(LocalDate.of(2024, 12, 15))
                .build();

        when(repository.save(any(Transaction.class))).thenReturn(saved);

        Transaction result = service.createTransaction(req);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getAmount()).isEqualByComparingTo("9.99");
        verify(repository, times(1)).save(any(Transaction.class));
    }
}