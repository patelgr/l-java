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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

final class MoneyTest {

    @Test
    void constructorNormalisesScale() {
        final Money money = new Money(new BigDecimal("10"), "USD");
        assertEquals("10.00 USD", money.toString());
    }

    @Test
    void constructorUppercasesCurrency() {
        final Money money = new Money(new BigDecimal("5.00"), "usd");
        assertEquals("USD", money.currency());
    }

    @Test
    void constructorRejectsNegativeAmount() {
        assertThrows(MoneyException.class, () -> new Money(new BigDecimal("-0.01"), "USD"));
    }

    @Test
    void ofParsesStringAmount() {
        final Money money = Money.of("12.50", "EUR");
        assertEquals(new BigDecimal("12.50"), money.amount());
    }

    @Test
    void ofThrowsOnInvalidAmount() {
        assertThrows(MoneyException.class, () -> Money.of("not-a-number", "USD"));
    }

    @Test
    void zeroIsZero() {
        assertTrue(Money.zero("USD").isZero());
    }

    @Test
    void addSameCurrency() {
        final Money a = Money.of("10.00", "USD");
        final Money b = Money.of("5.50", "USD");
        assertEquals(Money.of("15.50", "USD"), a.add(b));
    }

    @Test
    void addDifferentCurrencyThrows() {
        assertThrows(MoneyException.class, () -> Money.of("10.00", "USD").add(Money.of("5.00", "EUR")));
    }

    @Test
    void subtractProducesCorrectResult() {
        final Money a = Money.of("10.00", "USD");
        final Money b = Money.of("3.75", "USD");
        assertEquals(Money.of("6.25", "USD"), a.subtract(b));
    }

    @Test
    void subtractBelowZeroThrows() {
        assertThrows(MoneyException.class, () -> Money.of("3.00", "USD").subtract(Money.of("5.00", "USD")));
    }

    @Test
    void multiplyByFactor() {
        final Money money = Money.of("10.00", "USD");
        assertEquals(Money.of("25.00", "USD"), money.multiply(new BigDecimal("2.5")));
    }

    @Test
    void multiplyByNegativeThrows() {
        assertThrows(MoneyException.class, () -> Money.of("10.00", "USD").multiply(new BigDecimal("-1")));
    }

    @Test
    void equalityIgnoresTrailingZeros() {
        assertEquals(Money.of("10.00", "USD"), Money.of("10.0", "USD"));
    }

    @Test
    void inequalityOnDifferentCurrency() {
        assertNotEquals(Money.of("10.00", "USD"), Money.of("10.00", "EUR"));
    }

    @Test
    void isGreaterThan() {
        assertTrue(Money.of("10.00", "USD").isGreaterThan(Money.of("5.00", "USD")));
        assertFalse(Money.of("5.00", "USD").isGreaterThan(Money.of("10.00", "USD")));
    }
}
