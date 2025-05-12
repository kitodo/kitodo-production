/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class GuardTest {
    @Test
    @Tag("canCast")
    void canCastShouldCast() {
        Object input = "Hello world!";
        String greet = Guard.canCast("input", input, String.class);
        assertInstanceOf(String.class, greet, "should return String");
    }

    @Test
    @Tag("canCast")
    void canCastShouldCastToSuperclass() {
        Object input = new GregorianCalendar();
        Calendar calendar = Guard.canCast("input", input, Calendar.class);
        assertInstanceOf(Calendar.class, calendar, "should return Calendar");
    }

    @Test
    @Tag("canCast")
    void canCastShouldNotMiscast() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            Object input = new ArrayList<File>();
            Guard.canCast("input", input, String.class);
        }, "should throw IllegalArgumentException");
        assertEquals("java.util.ArrayList 'input' is not a (subclass of) java.lang.String", e.getMessage(),
            "should provide an understandable description");
    }

    @Test
    @Tag("isInRange")
    void isInRangeShouldFailBelowLowerBound() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            long dice = 0;
            Guard.isInRange("dice", dice, 1, 6);
        });
        assertEquals("'dice' out of range: 0 not in [1..6]", e.getMessage(),
            "should provide an understandable description");
    }

    @Test
    @Tag("isInRange")
    void isInRangeShouldNotFailOnLowerBound() {
        long dice = 1;
        Guard.isInRange("dice", dice, 1, 6);
    }

    @Test
    @Tag("isInRange")
    void isInRangeShouldNotFailInRange() {
        long dice = 3;
        Guard.isInRange("dice", dice, 1, 6);
    }

    @Test
    @Tag("isInRange")
    void isInRangeShouldNotFailOnUpperBound() {
        long dice = 6;
        Guard.isInRange("dice", dice, 1, 6);
    }

    @Test
    @Tag("isInRange")
    void isInRangeShouldFailAboveUpperBound() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            long dice = 7;
            Guard.isInRange("dice", dice, 1, 6);
        }, "should throw IllegalArgumentException");
        assertEquals("'dice' out of range: 7 not in [1..6]", e.getMessage(),
            "should provide an understandable description");
    }

    @Test
    @Tag("isNotNull")
    void isNotNullShouldNotFailForInitializedObjekt() {
        Object object = "Hello world!";
        Guard.isNotNull("object", object);
    }

    @Test
    @Tag("isNotNull")
    void isNotNullShouldFailForNullObjekt() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            Object object = null;
            Guard.isNotNull("object", object);
        }, "should throw IllegalArgumentException");
        assertEquals("'object' must not be null", e.getMessage(), "should provide an understandable description");
    }

    @Test
    @Tag("isPositive_double")
    void isPositiveDoubleShouldNotFailForPositiveValue() {
        double value = 42;
        Guard.isPositive("value", value);
    }

    @Test
    @Tag("isPositive_double")
    void isPositiveDoubleShouldNotFailForPositiveBoundary() {
        double value = Double.MIN_VALUE;
        Guard.isPositive("value", value);
    }

    @Test
    @Tag("isPositive_double")
    void isPositiveDoubleShouldFailForZero() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            double value = 0;
            Guard.isPositive("value", value);
        }, "should throw IllegalArgumentException");
        assertEquals("'value' out of range: 0.0 not > 0", e.getMessage(),
            "should provide an understandable description");
    }

    @Test
    @Tag("isPositive_double")
    void isPositiveDoubleShouldFailForNegative() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            double value = -299_792_458;
            Guard.isPositive("value", value);
        }, "should throw IllegalArgumentException");
        assertEquals("'value' out of range: -2.99792458E8 not > 0", e.getMessage(),
            "should provide an understandable description");
    }

    @Test
    @Tag("isPositive_long")
    void isPositiveLongShouldNotFailForPositiveUpperBound() {
        long value = Long.MAX_VALUE;
        Guard.isPositive("value", value);
    }

    @Test
    @Tag("isPositive_long")
    void isPositiveLongShouldNotFailForPositiveLowerBound() {
        long value = 1;
        Guard.isPositive("value", value);
    }

    @Test
    @Tag("isPositive_long")
    void isPositiveLongShouldFailForZero() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            long value = 0;
            Guard.isPositive("value", value);
        }, "should throw IllegalArgumentException");
        assertEquals("'value' out of range: 0 not > 0", e.getMessage(), "should provide an understandable description");
    }

    @Test
    @Tag("isPositive_long")
    void isPositiveLongShouldFailForNegative() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            long value = Long.MAX_VALUE + 1; // negative due to integer overflow
            Guard.isPositive("value", value);
        }, "should throw IllegalArgumentException");
        assertEquals("'value' out of range: -9223372036854775808 not > 0", e.getMessage(),
            "should provide an understandable description");
    }
}
