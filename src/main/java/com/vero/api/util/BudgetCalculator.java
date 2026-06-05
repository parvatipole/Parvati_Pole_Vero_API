package com.vero.api.util;

import com.vero.api.model.Category;
import com.vero.api.model.Transaction;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BudgetCalculator {

    private BudgetCalculator() {
    }

    /**
     * Groups the provided transactions by category, sums the total amount spent
     * per category, and returns the top N categories by total spend in descending order.
     *
     * The returned map preserves insertion order and contains at most {@code topN} entries.
     * If there are fewer distinct categories than {@code topN}, all categories are returned.
     *
     * @param transactions the list of transactions to analyse; must not be null
     * @param topN         the maximum number of categories to return; must be greater than zero
     * @return a map of Category to total spend, sorted descending by spend, limited to topN entries
     */
    public static Map<Category, BigDecimal> getTopSpendingCategories(List<Transaction> transactions, int topN) {
        // TODO: implement
        return new LinkedHashMap<>();
    }
}
