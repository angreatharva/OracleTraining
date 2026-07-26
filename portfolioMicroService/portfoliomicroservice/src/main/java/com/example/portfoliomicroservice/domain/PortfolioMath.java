package com.example.portfoliomicroservice.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PortfolioMath {

    private static final int SCALE = 4;

    private PortfolioMath() {
    }

    public static BigDecimal weightedAverageCost(BigDecimal existingQuantity, BigDecimal existingAverageCost,
                                                 BigDecimal newQuantity, BigDecimal newAverageCost) {
        BigDecimal totalQuantity = safe(existingQuantity).add(safe(newQuantity));
        if (totalQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal totalCost = safe(existingQuantity).multiply(safe(existingAverageCost))
                .add(safe(newQuantity).multiply(safe(newAverageCost)));
        return totalCost.divide(totalQuantity, SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal marketValue(BigDecimal quantity, BigDecimal unitPrice) {
        return safe(quantity).multiply(safe(unitPrice)).setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal unrealizedGainLoss(BigDecimal quantity, BigDecimal averageCost, BigDecimal marketValue) {
        return safe(marketValue).subtract(safe(quantity).multiply(safe(averageCost))).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
