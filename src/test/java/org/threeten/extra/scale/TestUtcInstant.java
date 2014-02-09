/*
 * Copyright (c) 2007-present, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.threeten.extra.scale;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test UtcInstant.
 */
@Test
public class TestUtcInstant {

    private static final long SECS_PER_DAY = 24L * 60 * 60;
    private static final long NANOS_PER_SEC = 1000000000L;

    //-----------------------------------------------------------------------
    @Test
    public void test_interfaces() {
        assertTrue(Serializable.class.isAssignableFrom(UtcInstant.class));
        assertTrue(Comparable.class.isAssignableFrom(UtcInstant.class));
    }

    //-----------------------------------------------------------------------
    // serialization
    //-----------------------------------------------------------------------
    @Test
    public void test_deserialization() throws Exception {
        UtcInstant orginal = UtcInstant.ofModifiedJulianDay(2, 3);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(orginal);
        out.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);
        UtcInstant ser = (UtcInstant) in.readObject();
        assertEquals(UtcInstant.ofModifiedJulianDay(2, 3), ser);
    }

//    //-----------------------------------------------------------------------
//    // nowClock()
//    //-----------------------------------------------------------------------
//    @Test(expectedExceptions=NullPointerException.class)
//    public void now_Clock_nullClock() {
//        TaiInstant.now(null);
//    }
//
//    public void now_TimeSource_allSecsInDay_utc() {
//        for (int i = 0; i < (2 * 24 * 60 * 60); i++) {
//            TaiInstant expected = TaiInstant.ofEpochSecond(i).plusNanos(123456789L);
//            TimeSource clock = TimeSource.fixed(expected);
//            TaiInstant test = TaiInstant.now(clock);
//            assertEquals(test, expected);
//        }
//    }
//
//    public void now_TimeSource_allSecsInDay_beforeEpoch() {
//        for (int i =-1; i >= -(24 * 60 * 60); i--) {
//            TaiInstant expected = TaiInstant.ofEpochSecond(i).plusNanos(123456789L);
//            TimeSource clock = TimeSource.fixed(expected);
//            TaiInstant test = TaiInstant.now(clock);
//            assertEquals(test, expected);
//        }
//    }
//
//    //-----------------------------------------------------------------------
//    // nowSystemClock()
//    //-----------------------------------------------------------------------
//    public void nowSystemClock() {
//        TaiInstant expected = TaiInstant.now(TimeSource.system());
//        TaiInstant test = TaiInstant.nowSystemClock();
//        BigInteger diff = test.toEpochNano().subtract(expected.toEpochNano()).abs();
//        if (diff.compareTo(BigInteger.valueOf(100000000)) >= 0) {
//            // may be date change
//            expected = TaiInstant.now(TimeSource.system());
//            test = TaiInstant.nowSystemClock();
//            diff = test.toEpochNano().subtract(expected.toEpochNano()).abs();
//        }
//        assertTrue(diff.compareTo(BigInteger.valueOf(100000000)) < 0);  // less than 0.1 secs
//    }

    //-----------------------------------------------------------------------
    // ofModififiedJulianDay(long,long)
    //-----------------------------------------------------------------------
    @Test
    public void factory_ofModifiedJulianDay_long_long() {
        for (long i = -2; i <= 2; i++) {
            for (int j = 0; j < 10; j++) {
                UtcInstant t = UtcInstant.ofModifiedJulianDay(i, j);
                assertEquals(t.getModifiedJulianDay(), i);
                assertEquals(t.getNanoOfDay(), j);
                assertEquals(t.getRules(), UtcRules.system());
                assertEquals(t.isLeapSecond(), false);
            }
        }
    }

    @Test
    public void factory_ofModifiedJulianDay_long_long_setupLeap() {
        MockUtcRulesAlwaysLeap mockRules = new MockUtcRulesAlwaysLeap();
        UtcInstant t = UtcInstant.ofModifiedJulianDay(41683 - 1, SECS_PER_DAY * NANOS_PER_SEC + 2, mockRules);
        assertEquals(t.getModifiedJulianDay(), 41683 - 1);
        assertEquals(t.getNanoOfDay(), SECS_PER_DAY * NANOS_PER_SEC + 2);
        assertEquals(t.getRules(), mockRules);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void factory_ofModifiedJulianDay_long_long_nanosNegative() {
        UtcInstant.ofModifiedJulianDay(2L, -1);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void factory_ofModifiedJulianDay_long_long_nanosTooBigNotLeapDay() {
        UtcInstant.ofModifiedJulianDay(2L, SECS_PER_DAY * NANOS_PER_SEC);
    }

    //-----------------------------------------------------------------------
    // ofModififiedJulianDay(long,long,Rules)
    //-----------------------------------------------------------------------
    @Test
    public void factory_ofModifiedJulianDay_long_long_Rules() {
        MockUtcRulesAlwaysLeap mockRules = new MockUtcRulesAlwaysLeap();
        for (long i = -2; i <= 2; i++) {
            for (int j = 0; j < 10; j++) {
                UtcInstant t = UtcInstant.ofModifiedJulianDay(i, j, mockRules);
                assertEquals(t.getModifiedJulianDay(), i);
                assertEquals(t.getNanoOfDay(), j);
                assertEquals(t.getRules(), mockRules);
                assertEquals(t.isLeapSecond(), false);
            }
        }
    }

    @Test
    public void factory_ofModifiedJulianDay_long_long_Rules_setupLeap() {
        MockUtcRulesAlwaysLeap mockRules = new MockUtcRulesAlwaysLeap();
        UtcInstant t = UtcInstant.ofModifiedJulianDay(0, SECS_PER_DAY * NANOS_PER_SEC + 2, mockRules);
        assertEquals(t.getModifiedJulianDay(), 0);
        assertEquals(t.getNanoOfDay(), SECS_PER_DAY * NANOS_PER_SEC + 2);
        assertEquals(t.getRules(), mockRules);
        assertEquals(t.isLeapSecond(), true);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void factory_ofModifiedJulianDay_long_long_Rules_nanosNegative() {
        MockUtcRulesAlwaysLeap mockRules = new MockUtcRulesAlwaysLeap();
        UtcInstant.ofModifiedJulianDay(2L, -1, mockRules);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void factory_ofModifiedJulianDay_long_long_Rules_nanosTooBigNotDoubleLeapDay() {
        MockUtcRulesAlwaysLeap mockRules = new MockUtcRulesAlwaysLeap();
        UtcInstant.ofModifiedJulianDay(2L, (SECS_PER_DAY + 1) * NANOS_PER_SEC, mockRules);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_ofModifiedJulianDay_long_long_Rules_null() {
        UtcInstant.ofModifiedJulianDay(0, 0, (UtcRules) null);
    }

    //-----------------------------------------------------------------------
    // of(Instant)
    //-----------------------------------------------------------------------
    @Test
    public void factory_of_Instant() {
        UtcInstant test = UtcInstant.of(Instant.ofEpochSecond(0, 2));  // 1970-01-01
        assertEquals(test.getModifiedJulianDay(), 40587);
        assertEquals(test.getNanoOfDay(), 2);
        assertEquals(test.getRules(), UtcRules.system());
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_of_Instant_null() {
        UtcInstant.of((Instant) null);
    }

    //-----------------------------------------------------------------------
    // of(Instant, LeapSecondRules)
    //-----------------------------------------------------------------------
    @Test
    public void factory_of_Instant_Rules() {
        MockUtcRulesAlwaysLeap mockRules = new MockUtcRulesAlwaysLeap();
        UtcInstant test = UtcInstant.of(Instant.ofEpochSecond(0, 2), mockRules);  // 1970-01-01
        assertEquals(test.getModifiedJulianDay(), 40587);
        assertEquals(test.getNanoOfDay(), 2);
        assertEquals(test.getRules(), mockRules);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_of_Instant_Rules_null() {
        UtcInstant.of(Instant.ofEpochSecond(0, 2), (UtcRules) null);
    }

    //-----------------------------------------------------------------------
    // of(TaiInstant)
    //-----------------------------------------------------------------------
    @Test
    public void factory_of_TaiInstant() {
        for (int i = -1000; i < 1000; i++) {
            for (int j = 0; j < 10; j++) {
                UtcInstant expected = UtcInstant.ofModifiedJulianDay(36204 + i, j * NANOS_PER_SEC + 2L);
                TaiInstant tai = TaiInstant.ofTaiSeconds(i * SECS_PER_DAY + j + 10, 2);
                assertEquals(UtcInstant.of(tai), expected);
            }
        }
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_of_TaiInstant_null() {
        UtcInstant.of((TaiInstant) null);
    }

    //-----------------------------------------------------------------------
    // of(TaiInstant, LeapSecondRules)
    //-----------------------------------------------------------------------
    @Test
    public void factory_of_TaiInstant_Rules() {
        TaiInstant tai = TaiInstant.ofTaiSeconds(2 * SECS_PER_DAY + 10, 2);
        UtcInstant test = UtcInstant.of(tai, UtcRules.system());
        assertEquals(test.getModifiedJulianDay(), 36204 + 2);
        assertEquals(test.getNanoOfDay(), 2);
        assertEquals(test.getRules(), UtcRules.system());
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_of_TaiInstant_Rules_null() {
        UtcInstant.of(TaiInstant.ofTaiSeconds(0, 2), (UtcRules) null);
    }

    //-----------------------------------------------------------------------
    // withModifiedJulianDay()
    //-----------------------------------------------------------------------
    @DataProvider(name="WithModifiedJulianDay")
    Object[][] provider_withModifiedJulianDay() {
        return new Object[][] {
            {0L, 12345L,  1L, 1L, 12345L},
            {0L, 12345L,  -1L, -1L, 12345L},
            {7L, 12345L,  2L, 2L, 12345L},
            {7L, 12345L,  -2L, -2L, 12345L},
            {-99L, 12345L,  3L, 3L, 12345L},
            {-99L, 12345L,  -3L, -3L, 12345L},
            {1000L, NANOS_PER_SEC * SECS_PER_DAY,  999L, null, null},
            {1000L, NANOS_PER_SEC * SECS_PER_DAY,  1000L, 1000L, NANOS_PER_SEC * SECS_PER_DAY},
            {1000L, NANOS_PER_SEC * SECS_PER_DAY,  1001L, null, null},
       };
    }

    @Test(dataProvider="WithModifiedJulianDay")
    public void test_withModifiedJulianDay(long mjd, long nanos, long newMjd, Long expectedMjd, Long expectedNanos) {
        UtcInstant i = UtcInstant.ofModifiedJulianDay(mjd, nanos, new MockUtcRulesLeapOn1000());
        if (expectedMjd != null) {
            i = i.withModifiedJulianDay(newMjd);
            assertEquals(i.getModifiedJulianDay(), expectedMjd.longValue());
            assertEquals(i.getNanoOfDay(), expectedNanos.longValue());
        } else {
            try {
                i = i.withModifiedJulianDay(newMjd);
                fail();
            } catch (IllegalArgumentException ex) {
                // expected
            }
        }
     }

    //-----------------------------------------------------------------------
    // withNanoOfDay()
    //-----------------------------------------------------------------------
    @DataProvider(name="WithNanoOfDay")
    Object[][] provider_withNanoOfDay() {
        return new Object[][] {
            {0L, 12345L,  1L, 0L, 1L},
            {0L, 12345L,  -1L, null, null},
            {7L, 12345L,  2L, 7L, 2L},
            {-99L, 12345L,  3L, -99L, 3L},
            {1000L, NANOS_PER_SEC * SECS_PER_DAY,  NANOS_PER_SEC * SECS_PER_DAY - 1, 1000L, NANOS_PER_SEC * SECS_PER_DAY - 1},
       };
    }

    @Test(dataProvider="WithNanoOfDay")
    public void test_withNanoOfDay(long mjd, long nanos, long newNanoOfDay, Long expectedMjd, Long expectedNanos) {
        UtcInstant i = UtcInstant.ofModifiedJulianDay(mjd, nanos, new MockUtcRulesLeapOn1000());
        if (expectedMjd != null) {
            i = i.withNanoOfDay(newNanoOfDay);
            assertEquals(i.getModifiedJulianDay(), expectedMjd.longValue());
            assertEquals(i.getNanoOfDay(), expectedNanos.longValue());
        } else {
            try {
                i = i.withNanoOfDay(newNanoOfDay);
                fail();
            } catch (IllegalArgumentException ex) {
                // expected
            }
        }
     }

    //-----------------------------------------------------------------------
    // plus(Duration)
    //-----------------------------------------------------------------------
    @DataProvider(name="Plus")
    Object[][] provider_plus() {
        return new Object[][] {
            {0, 0,  -2 * SECS_PER_DAY, 5, -2, 5},
            {0, 0,  -1 * SECS_PER_DAY, 1, -1, 1},
            {0, 0,  -1 * SECS_PER_DAY, 0, -1, 0},
            {0, 0,  0,        -2, -1,  SECS_PER_DAY * NANOS_PER_SEC - 2},
            {0, 0,  0,        -1, -1,  SECS_PER_DAY * NANOS_PER_SEC - 1},
            {0, 0,  0,         0,  0,  0},
            {0, 0,  0,         1,  0,  1},
            {0, 0,  0,         2,  0,  2},
            {0, 0,  1,         0,  0,  1 * NANOS_PER_SEC},
            {0, 0,  2,         0,  0,  2 * NANOS_PER_SEC},
            {0, 0,  3, 333333333,  0,  3 * NANOS_PER_SEC + 333333333},
            {0, 0,  1 * SECS_PER_DAY, 0,  1, 0},
            {0, 0,  1 * SECS_PER_DAY, 1,  1, 1},
            {0, 0,  2 * SECS_PER_DAY, 5,  2, 5},

            {1, 0,  -2 * SECS_PER_DAY, 5, -1, 5},
            {1, 0,  -1 * SECS_PER_DAY, 1, 0, 1},
            {1, 0,  -1 * SECS_PER_DAY, 0, 0, 0},
            {1, 0,  0,        -2,  0,  SECS_PER_DAY * NANOS_PER_SEC - 2},
            {1, 0,  0,        -1,  0,  SECS_PER_DAY * NANOS_PER_SEC - 1},
            {1, 0,  0,         0,  1,  0},
            {1, 0,  0,         1,  1,  1},
            {1, 0,  0,         2,  1,  2},
            {1, 0,  1,         0,  1,  1 * NANOS_PER_SEC},
            {1, 0,  2,         0,  1,  2 * NANOS_PER_SEC},
            {1, 0,  3, 333333333,  1,  3 * NANOS_PER_SEC + 333333333},
            {1, 0,  1 * SECS_PER_DAY, 0,  2, 0},
            {1, 0,  1 * SECS_PER_DAY, 1,  2, 1},
            {1, 0,  2 * SECS_PER_DAY, 5,  3, 5},
       };
    }

    @Test(dataProvider="Plus")
    public void test_plus(long mjd, long nanos, long plusSeconds, int plusNanos, long expectedMjd, long expectedNanos) {
       UtcInstant i = UtcInstant.ofModifiedJulianDay(mjd, nanos).plus(Duration.ofSeconds(plusSeconds, plusNanos));
       assertEquals(i.getModifiedJulianDay(), expectedMjd);
       assertEquals(i.getNanoOfDay(), expectedNanos);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_plus_overflowTooBig() {
       UtcInstant i = UtcInstant.ofModifiedJulianDay(Long.MAX_VALUE, SECS_PER_DAY * NANOS_PER_SEC - 1);
       i.plus(Duration.ofNanos(1));
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_plus_overflowTooSmall() {
       UtcInstant i = UtcInstant.ofModifiedJulianDay(Long.MIN_VALUE, 0);
       i.plus(Duration.ofNanos(-1));
    }

    //-----------------------------------------------------------------------
    // minus(Duration)
    //-----------------------------------------------------------------------
    @DataProvider(name="Minus")
    Object[][] provider_minus() {
        return new Object[][] {
            {0, 0,  2 * SECS_PER_DAY, -5, -2, 5},
            {0, 0,  1 * SECS_PER_DAY, -1, -1, 1},
            {0, 0,  1 * SECS_PER_DAY, 0, -1, 0},
            {0, 0,  0,          2, -1,  SECS_PER_DAY * NANOS_PER_SEC - 2},
            {0, 0,  0,          1, -1,  SECS_PER_DAY * NANOS_PER_SEC - 1},
            {0, 0,  0,          0,  0,  0},
            {0, 0,  0,         -1,  0,  1},
            {0, 0,  0,         -2,  0,  2},
            {0, 0,  -1,         0,  0,  1 * NANOS_PER_SEC},
            {0, 0,  -2,         0,  0,  2 * NANOS_PER_SEC},
            {0, 0,  -3, -333333333,  0,  3 * NANOS_PER_SEC + 333333333},
            {0, 0,  -1 * SECS_PER_DAY, 0,  1, 0},
            {0, 0,  -1 * SECS_PER_DAY, -1,  1, 1},
            {0, 0,  -2 * SECS_PER_DAY, -5,  2, 5},

            {1, 0,  2 * SECS_PER_DAY, -5, -1, 5},
            {1, 0,  1 * SECS_PER_DAY, -1, 0, 1},
            {1, 0,  1 * SECS_PER_DAY, 0, 0, 0},
            {1, 0,  0,          2,  0,  SECS_PER_DAY * NANOS_PER_SEC - 2},
            {1, 0,  0,          1,  0,  SECS_PER_DAY * NANOS_PER_SEC - 1},
            {1, 0,  0,          0,  1,  0},
            {1, 0,  0,         -1,  1,  1},
            {1, 0,  0,         -2,  1,  2},
            {1, 0,  -1,         0,  1,  1 * NANOS_PER_SEC},
            {1, 0,  -2,         0,  1,  2 * NANOS_PER_SEC},
            {1, 0,  -3, -333333333,  1,  3 * NANOS_PER_SEC + 333333333},
            {1, 0,  -1 * SECS_PER_DAY, 0,  2, 0},
            {1, 0,  -1 * SECS_PER_DAY, -1,  2, 1},
            {1, 0,  -2 * SECS_PER_DAY, -5,  3, 5},
       };
    }

    @Test(dataProvider="Minus")
    public void test_minus(long mjd, long nanos, long minusSeconds, int minusNanos, long expectedMjd, long expectedNanos) {
       UtcInstant i = UtcInstant.ofModifiedJulianDay(mjd, nanos).minus(Duration.ofSeconds(minusSeconds, minusNanos));
       assertEquals(i.getModifiedJulianDay(), expectedMjd);
       assertEquals(i.getNanoOfDay(), expectedNanos);
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_minus_overflowTooSmall() {
       UtcInstant i = UtcInstant.ofModifiedJulianDay(Long.MIN_VALUE, 0);
       i.minus(Duration.ofNanos(1));
    }

    @Test(expectedExceptions=ArithmeticException.class)
    public void test_minus_overflowTooBig() {
       UtcInstant i = UtcInstant.ofModifiedJulianDay(Long.MAX_VALUE, SECS_PER_DAY * NANOS_PER_SEC - 1);
       i.minus(Duration.ofNanos(-1));
    }

    //-----------------------------------------------------------------------
    // durationUntil()
    //-----------------------------------------------------------------------
    @Test
    public void test_durationUntil_oneDayNoLeap() {
        UtcInstant utc1 = UtcInstant.ofModifiedJulianDay(41681, 0);  // 1972-12-30
        UtcInstant utc2 = UtcInstant.ofModifiedJulianDay(41682, 0);  // 1972-12-31
        Duration test = utc1.durationUntil(utc2);
        assertEquals(test.getSeconds(), 86400);
        assertEquals(test.getNano(), 0);
    }

    @Test
    public void test_durationUntil_oneDayLeap() {
        UtcInstant utc1 = UtcInstant.ofModifiedJulianDay(41682, 0);  // 1972-12-31
        UtcInstant utc2 = UtcInstant.ofModifiedJulianDay(41683, 0);  // 1973-01-01
        Duration test = utc1.durationUntil(utc2);
        assertEquals(test.getSeconds(), 86401);
        assertEquals(test.getNano(), 0);
    }

    @Test
    public void test_durationUntil_oneDayLeapNegative() {
        UtcInstant utc1 = UtcInstant.ofModifiedJulianDay(41683, 0);  // 1973-01-01
        UtcInstant utc2 = UtcInstant.ofModifiedJulianDay(41682, 0);  // 1972-12-31
        Duration test = utc1.durationUntil(utc2);
        assertEquals(test.getSeconds(), -86401);
        assertEquals(test.getNano(), 0);
    }

    //-----------------------------------------------------------------------
    // toTaiInstant()
    //-----------------------------------------------------------------------
    @Test
    public void test_toTaiInstant() {
        for (int i = -1000; i < 1000; i++) {
            for (int j = 0; j < 10; j++) {
                UtcInstant utc = UtcInstant.ofModifiedJulianDay(36204 + i, j * NANOS_PER_SEC + 2L);
                TaiInstant test = utc.toTaiInstant();
                assertEquals(test.getTaiSeconds(), i * SECS_PER_DAY + j + 10);
                assertEquals(test.getNano(), 2);
            }
        }
    }

    //-----------------------------------------------------------------------
    // toInstant()
    //-----------------------------------------------------------------------
    @Test
    public void test_toInstant() {
        for (int i = -1000; i < 1000; i++) {
            for (int j = 0; j < 10; j++) {
                Instant expected = Instant.ofEpochSecond(315532800 + i * SECS_PER_DAY + j).plusNanos(2);
                UtcInstant test = UtcInstant.ofModifiedJulianDay(44239 + i, j * NANOS_PER_SEC + 2);
                assertEquals(test.toInstant(), expected, "Loop " + i + " " + j);
            }
        }
    }

    //-----------------------------------------------------------------------
    // compareTo()
    //-----------------------------------------------------------------------
    @Test
    public void test_comparisons() {
        doTest_comparisons_UtcInstant(
            UtcInstant.ofModifiedJulianDay(-2L, 0),
            UtcInstant.ofModifiedJulianDay(-2L, SECS_PER_DAY * NANOS_PER_SEC - 2),
            UtcInstant.ofModifiedJulianDay(-2L, SECS_PER_DAY * NANOS_PER_SEC - 1),
            UtcInstant.ofModifiedJulianDay(-1L, 0),
            UtcInstant.ofModifiedJulianDay(-1L, 1),
            UtcInstant.ofModifiedJulianDay(-1L, SECS_PER_DAY * NANOS_PER_SEC - 2),
            UtcInstant.ofModifiedJulianDay(-1L, SECS_PER_DAY * NANOS_PER_SEC - 1),
            UtcInstant.ofModifiedJulianDay(0L, 0),
            UtcInstant.ofModifiedJulianDay(0L, 1),
            UtcInstant.ofModifiedJulianDay(0L, 2),
            UtcInstant.ofModifiedJulianDay(0L, SECS_PER_DAY * NANOS_PER_SEC - 1),
            UtcInstant.ofModifiedJulianDay(1L, 0),
            UtcInstant.ofModifiedJulianDay(2L, 0)
        );
    }

    void doTest_comparisons_UtcInstant(UtcInstant... instants) {
        for (int i = 0; i < instants.length; i++) {
            UtcInstant a = instants[i];
            for (int j = 0; j < instants.length; j++) {
                UtcInstant b = instants[j];
                if (i < j) {
                    assertEquals(a.compareTo(b), -1, a + " <=> " + b);
                    assertEquals(a.equals(b), false, a + " <=> " + b);
                } else if (i > j) {
                    assertEquals(a.compareTo(b), 1, a + " <=> " + b);
                    assertEquals(a.equals(b), false, a + " <=> " + b);
                } else {
                    assertEquals(a.compareTo(b), 0, a + " <=> " + b);
                    assertEquals(a.equals(b), true, a + " <=> " + b);
                }
            }
        }
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_compareTo_ObjectNull() {
        UtcInstant a = UtcInstant.ofModifiedJulianDay(0L, 0);
        a.compareTo(null);
    }

    @Test(expectedExceptions=ClassCastException.class)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void test_compareToNonUtcInstant() {
       Comparable c = UtcInstant.ofModifiedJulianDay(0L, 2);
       c.compareTo(new Object());
    }

    //-----------------------------------------------------------------------
    // equals()
    //-----------------------------------------------------------------------
    @Test
    public void test_equals() {
        UtcInstant test5a = UtcInstant.ofModifiedJulianDay(5L, 20);
        UtcInstant test5b = UtcInstant.ofModifiedJulianDay(5L, 20);
        UtcInstant test5n = UtcInstant.ofModifiedJulianDay(5L, 30);
        UtcInstant test6 = UtcInstant.ofModifiedJulianDay(6L, 20);

        assertEquals(test5a.equals(test5a), true);
        assertEquals(test5a.equals(test5b), true);
        assertEquals(test5a.equals(test5n), false);
        assertEquals(test5a.equals(test6), false);

        assertEquals(test5b.equals(test5a), true);
        assertEquals(test5b.equals(test5b), true);
        assertEquals(test5b.equals(test5n), false);
        assertEquals(test5b.equals(test6), false);

        assertEquals(test5n.equals(test5a), false);
        assertEquals(test5n.equals(test5b), false);
        assertEquals(test5n.equals(test5n), true);
        assertEquals(test5n.equals(test6), false);

        assertEquals(test6.equals(test5a), false);
        assertEquals(test6.equals(test5b), false);
        assertEquals(test6.equals(test5n), false);
        assertEquals(test6.equals(test6), true);
    }

    @Test
    public void test_equals_null() {
        UtcInstant test5 = UtcInstant.ofModifiedJulianDay(5L, 20);
        assertEquals(test5.equals(null), false);
    }

    @Test
    public void test_equals_otherClass() {
        UtcInstant test5 = UtcInstant.ofModifiedJulianDay(5L, 20);
        assertEquals(test5.equals(""), false);
    }

    //-----------------------------------------------------------------------
    // hashCode()
    //-----------------------------------------------------------------------
    @Test
    public void test_hashCode() {
        UtcInstant test5a = UtcInstant.ofModifiedJulianDay(5L, 20);
        UtcInstant test5b = UtcInstant.ofModifiedJulianDay(5L, 20);
        UtcInstant test5n = UtcInstant.ofModifiedJulianDay(5L, 30);
        UtcInstant test6 = UtcInstant.ofModifiedJulianDay(6L, 20);

        assertEquals(test5a.hashCode() == test5a.hashCode(), true);
        assertEquals(test5a.hashCode() == test5b.hashCode(), true);
        assertEquals(test5b.hashCode() == test5b.hashCode(), true);

        assertEquals(test5a.hashCode() == test5n.hashCode(), false);
        assertEquals(test5a.hashCode() == test6.hashCode(), false);
    }

    //-----------------------------------------------------------------------
    // toString()
    //-----------------------------------------------------------------------
    @Test
    public void test_toString() {
        assertEquals(UtcInstant.ofModifiedJulianDay(40587, 0).toString(), "1970-01-01T00:00:00.000000000(UTC)");
        assertEquals(UtcInstant.ofModifiedJulianDay(40588, 1).toString(), "1970-01-02T00:00:00.000000001(UTC)");
        assertEquals(UtcInstant.ofModifiedJulianDay(40618, 999999999).toString(), "1970-02-01T00:00:00.999999999(UTC)");
        assertEquals(UtcInstant.ofModifiedJulianDay(40619, 1000000000).toString(), "1970-02-02T00:00:01.000000000(UTC)");
        assertEquals(UtcInstant.ofModifiedJulianDay(40620, 60L * 1000000000L).toString(), "1970-02-03T00:01:00.000000000(UTC)");
        assertEquals(UtcInstant.ofModifiedJulianDay(40621, 60L * 60L * 1000000000L).toString(), "1970-02-04T01:00:00.000000000(UTC)");
    }

    @Test
    public void test_toString_leap() {
        assertEquals(UtcInstant.ofModifiedJulianDay(41682, 24L * 60L * 60L * 1000000000L - 1000000000L).toString(), "1972-12-31T23:59:59.000000000(UTC)");
        assertEquals(UtcInstant.ofModifiedJulianDay(41682, 24L * 60L * 60L * 1000000000L).toString(), "1972-12-31T23:59:60.000000000(UTC)");
        assertEquals(UtcInstant.ofModifiedJulianDay(41683, 0).toString(), "1973-01-01T00:00:00.000000000(UTC)");
    }

}