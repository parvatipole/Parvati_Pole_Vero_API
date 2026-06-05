package com.vero.api.util;

import com.vero.api.model.Category;
import com.vero.api.model.Transaction;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FIX (Bug 5): This class was referenced in TransactionServiceImpl but was never
 * provided in the codebase, causing a compilation failure.
 *
 * Utility class for budget-related calculations. Kept as a stateless helper with
 * only static methods — it holds no state and has no Spring dependencies, so
 * making it a @Service would be unnecessary overhead.
 */
public final class BudgetCalculator {

    // Prevent instantiation — this is a pure utility class
    private BudgetCalculator() {}

    /**
     * Returns the top {@code topN} spending categories by total amount, in descending order,
     * computed over the supplied list of transactions.
     *
     * <p>Steps:
     * <ol>
     *   <li>Group all transactions by category and sum their amounts.</li>
     *   <li>Sort the resulting entries by total spend, highest first.</li>
     *   <li>Take at most {@code topN} entries.</li>
     *   <li>Collect into a LinkedHashMap to preserve descending order for the caller.</li>
     * </ol>
     *
     * @param transactions the full list of transactions to aggregate (may be empty)
     * @param topN         the maximum number of categories to return; if fewer categories
     *                     exist than topN, all categories are returned
     * @return a map of category → total spend, ordered highest to lowest, never null
     */
    public static Map<Category, BigDecimal> getTopSpendingCategories(
            List<Transaction> transactions, int topN) {

        if (transactions == null || transactions.isEmpty()) {
            return new LinkedHashMap<>();
        }

        return transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<Category, BigDecimal>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        BigDecimal::add,
                        LinkedHashMap::new   // preserves descending insertion order
                ));
    }
}