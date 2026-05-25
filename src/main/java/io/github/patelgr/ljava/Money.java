/*
 * Copyright 2026 Author Name
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.patelgr.ljava;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

/**
 * Immutable monetary amount with a currency code.
 *
 * <p>All arithmetic preserves the scale of the operands and rounds using
 * {@link RoundingMode#HALF_EVEN} (banker's rounding) to minimise cumulative error.
 */
public final class Money {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    private final BigDecimal amount;
    private final String currency;

    /**
     * Creates a monetary amount.
     *
     * @param amount a non-null, non-negative value
     * @param currency ISO 4217 currency code, e.g. {@code "USD"}
     * @throws MoneyException if {@code amount} is negative
     */
    public Money(final BigDecimal amount, final String currency) {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new MoneyException("amount must not be negative: " + amount);
        }
        this.amount = amount.setScale(SCALE, ROUNDING);
        this.currency = currency.toUpperCase(Locale.ROOT);
    }

    /** Factory method for convenience — parses a string amount. */
    public static Money of(final String amount, final String currency) {
        try {
            return new Money(new BigDecimal(amount), currency);
        } catch (final NumberFormatException e) {
            throw new MoneyException("invalid amount: " + amount, e);
        }
    }

    /** Returns a zero amount in the given currency. */
    public static Money zero(final String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    /** Returns the numeric amount. */
    public BigDecimal amount() {
        return this.amount;
    }

    /** Returns the ISO 4217 currency code. */
    public String currency() {
        return this.currency;
    }

    /**
     * Adds another monetary amount.
     *
     * @throws MoneyException if the currencies differ
     */
    public Money add(final Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Subtracts another monetary amount.
     *
     * @throws MoneyException if the currencies differ or the result would be negative
     */
    public Money subtract(final Money other) {
        requireSameCurrency(other);
        final BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new MoneyException("subtraction would produce a negative amount: " + result);
        }
        return new Money(result, this.currency);
    }

    /**
     * Multiplies by a scalar factor.
     *
     * @param factor must be non-negative
     * @throws MoneyException if {@code factor} is negative
     */
    public Money multiply(final BigDecimal factor) {
        Objects.requireNonNull(factor, "factor must not be null");
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new MoneyException("factor must not be negative: " + factor);
        }
        return new Money(this.amount.multiply(factor).setScale(SCALE, ROUNDING), this.currency);
    }

    /** Returns {@code true} if this amount is zero. */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /** Returns {@code true} if this amount is greater than {@code other}. */
    public boolean isGreaterThan(final Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Money other)) {
            return false;
        }
        return this.amount.compareTo(other.amount) == 0 && this.currency.equals(other.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.amount.stripTrailingZeros(), this.currency);
    }

    @Override
    public String toString() {
        return this.amount.toPlainString() + " " + this.currency;
    }

    private void requireSameCurrency(final Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new MoneyException("currency mismatch: " + this.currency + " vs " + other.currency);
        }
    }
}
