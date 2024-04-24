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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

public class GuardTest {

    @Test
    public void canCastTest() {
        // valid cast
        try {
            Object input = "Hello world!";
            String greet = Guard.canCast("input", input, String.class);
            assertTrue("should return String", greet instanceof String);
        } catch (IllegalArgumentException e) {
            fail("should return without exception");
        }

        // valid cast to superclass
        try {
            Object input = new GregorianCalendar();
            Calendar calendar = Guard.canCast("input", input, Calendar.class);
            assertTrue("should return Calendar", calendar instanceof Calendar);
        } catch (IllegalArgumentException e) {
            fail("should return without exception");
        }

        // invalid cast
        try {
            Object input = new ArrayList<File>();
            @SuppressWarnings("unused") String uncastable = Guard.canCast("input", input, String.class);
            fail("should have throws IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("java.util.ArrayList 'input' is not a (subclass of) java.lang.String", e.getMessage());
        }
    }

    @Test
    public void isInRangeTest() {
        // input below range
        try {
            long input = 0;
            Guard.isInRange("input", input, 1, 6);
            fail("should have throws IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("'input' out of range: 0 not in [1..6]", e.getMessage());
        }

        // input OK (lower bound)
        try {
            long input = 1;
            Guard.isInRange("input", input, 1, 6);
        } catch (IllegalArgumentException e) {
            fail("should return without exception");
        }

        // input OK (middle)
        try {
            long input = 3;
            Guard.isInRange("input", input, 1, 6);
        } catch (IllegalArgumentException e) {
            fail("should return without exception");
        }

        // input OK (upper bound)
        try {
            long input = 6;
            Guard.isInRange("input", input, 1, 6);
        } catch (IllegalArgumentException e) {
            fail("should return without exception");
        }

        // input above range
        try {
            long input = 42;
            Guard.isInRange("input", input, 1, 6);
            fail("should have throws IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("'input' out of range: 42 not in [1..6]", e.getMessage());
        }
    }

    @Test
    public void isNotNullTest() {
        // input is not null
        try {
            Object input = "Hello world!";
            Guard.isNotNull("input", input);
        } catch (IllegalArgumentException e) {
            fail("should return without exception");
        }

        // input is null
        try {
            Object input = null;
            Guard.isNotNull("input", input);
            fail("should have throws IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("'input' must not be null", e.getMessage());
        }
    }

    @Test
    public void isPositiveDoubleTest() {
        // input is positive
        try {
            double value = 42;
            Guard.isPositive("value", value);
        } catch (IllegalArgumentException e) {
            fail("should return without exception");
        }

        // input is still positive, but very very tiny (boundary)
        try {
            double value = Double.MIN_VALUE;
            Guard.isPositive("value", value);
        } catch (IllegalArgumentException e) {
            fail("should return without exception");
        }

        // input is zero (boundary)
        try {
            double value = 0;
            Guard.isPositive("value", value);
            fail("should have throws IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("'value' out of range: 0.0 not > 0", e.getMessage());
        }

        // input is negative light speed
        try {
            double value = -299_792_458;
            Guard.isPositive("value", value);
            fail("should have throws IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("'value' out of range: -2.99792458E8 not > 0", e.getMessage());
        }
    }

    @Test
    public void isPositiveLongTest() {
        // input is positive
        try {
            long value = Long.MAX_VALUE;
            Guard.isPositive("value", value);
        } catch (IllegalArgumentException e) {
            fail("should return without exception");
        }

        // input is one (boundary)
        try {
            long value = 1;
            Guard.isPositive("value", value);
        } catch (IllegalArgumentException e) {
            fail("should return without exception");
        }

        // input is zero (boundary)
        try {
            long value = 0;
            Guard.isPositive("value", value);
            fail("should have throws IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("'value' out of range: 0 not > 0", e.getMessage());
        }

        // input is negative (due to integer overflow)
        try {
            long value = Long.MAX_VALUE + 1;
            Guard.isPositive("value", value);
            fail("should have throws IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("'value' out of range: -9223372036854775808 not > 0", e.getMessage());
        }
    }
}
