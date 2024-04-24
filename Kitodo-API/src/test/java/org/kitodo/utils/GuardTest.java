/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

public class GuardTest {

    @Test
    public void canCastShouldCast() {
        Object input = "Hello world!";
        String greet = Guard.canCast("input", input, String.class);
        assertTrue("should return String", greet instanceof String);
    }

    @Test
    public void canCastShouldCastToSuperclass() {
        Object input = new GregorianCalendar();
        Calendar calendar = Guard.canCast("input", input, Calendar.class);
        assertTrue("should return Calendar", calendar instanceof Calendar);
    }

    @Test
    public void canCastShouldNotMiscast() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            Object input = new ArrayList<File>();
            Guard.canCast("input", input, String.class);
        });
        assertEquals("java.util.ArrayList 'input' is not a (subclass of) java.lang.String", e.getMessage());
    }


    @Test
    public void isInRangeShouldFailBelowLowerBound() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            long dice = 0;
            Guard.isInRange("dice", dice, 1, 6);
        });
        assertEquals("'dice' out of range: 0 not in [1..6]", e.getMessage());
    }


    @Test
    public void isInRangeShouldNotFailOnLowerBound() {
        long dice = 1;
        Guard.isInRange("dice", dice, 1, 6);
    }

    @Test
    public void isInRangeShouldNotFailInRange() {
        long dice = 3;
        Guard.isInRange("dice", dice, 1, 6);
    }

    @Test
    public void isInRangeShouldNotFailOnUpperBound() {
        long dice = 6;
        Guard.isInRange("dice", dice, 1, 6);
    }

    @Test
    public void isInRangeShouldFailAboveUpperBound() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            long dice = 7;
            Guard.isInRange("dice", dice, 1, 6);
        });
        assertEquals("'dice' out of range: 7 not in [1..6]", e.getMessage());
    }

    @Test
    public void isNotNullShouldNotFailForInitializedObjekt() {
        Object object = "Hello world!";
        Guard.isNotNull("object", object);
    }

    @Test
    public void isNotNullShouldFailForNullObjekt() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            Object object = null;
            Guard.isNotNull("object", object);
        });
        assertEquals("'object' must not be null", e.getMessage());
    }

    @Test
    public void isPositiveDoubleShouldNotFailForPositiveValue() {
        double value = 42;
        Guard.isPositive("value", value);
    }

    @Test
    public void isPositiveDoubleShouldNotFailForPositiveBoundary() {
        double value = Double.MIN_VALUE;
        Guard.isPositive("value", value);
    }

    @Test
    public void isPositiveDoubleShouldFailForZero() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            double value = 0;
            Guard.isPositive("value", value);
        });
        assertEquals("'value' out of range: 0.0 not > 0", e.getMessage());
    }

    @Test
    public void isPositiveDoubleShouldFailForNegative() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            double value = -299_792_458;
            Guard.isPositive("value", value);
        });
        assertEquals("'value' out of range: -2.99792458E8 not > 0", e.getMessage());
    }

    @Test
    public void isPositiveLongShouldNotFailForPositiveUpperBound() {
        long value = Long.MAX_VALUE;
        Guard.isPositive("value", value);
    }

    @Test
    public void isPositiveLongShouldNotFailForPositiveLowerBound() {
        long value = 1;
        Guard.isPositive("value", value);
    }

    @Test
    public void isPositiveLongShouldFailForZero() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            long value = 0;
            Guard.isPositive("value", value);
        });
        assertEquals("'value' out of range: 0 not > 0", e.getMessage());
    }

    @Test
    public void isPositiveLongShouldFailForNegative() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            long value = Long.MAX_VALUE + 1; // negative due to integer overflow
            Guard.isPositive("value", value);
        });
        assertEquals("'value' out of range: -9223372036854775808 not > 0", e.getMessage());
    }
}
