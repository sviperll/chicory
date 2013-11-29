/*
 * Copyright (c) 2013, Victor Nazarov <asviraspossible@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Victor Nazarov nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.sviperll.time;

import org.junit.Test;
import static org.junit.Assert.*;

public class TimesTest {
    @Test
    public void testUnixTimeToHumanTimeConversion() throws Exception {
        TimeZoneOffset moscow = new TimeZoneOffset(4 * 60);
        UnixTime unix = new UnixTime(1360073387593L);
        Day day = new Day(2013, new YearDay(Month.FEBRUARY, 5), WeekDay.TUESDAY);
        ClockTime clockTime = new ClockTime(18, 9, 47, 593);
        HumanTime humanExpected = new HumanTime(day, clockTime, moscow);
        HumanTime humanActual = Times.getHumanTime(unix, moscow);
        assertEquals(humanExpected.clockTime(), humanActual.clockTime());
        assertEquals(humanExpected.offset(), humanActual.offset());
        assertEquals(humanExpected.day().weekDay(), humanActual.day().weekDay());
        assertEquals(humanExpected.day().year(), humanActual.day().year());
        assertEquals(humanExpected.day().yearDay(), humanActual.day().yearDay());
    }

    @Test
    public void testHumanTimeToUnixTimeConversion() throws Exception {
        TimeZoneOffset moscow = new TimeZoneOffset(4 * 60);
        Day day = new Day(2013, new YearDay(Month.FEBRUARY, 5), WeekDay.TUESDAY);
        ClockTime clockTime = new ClockTime(18, 9, 47, 593);
        HumanTime human = new HumanTime(day, clockTime, moscow);
        UnixTime unixExpected = new UnixTime(1360073387593L);
        UnixTime unixActual = Times.getUnixTime(human);
        assertEquals(unixExpected, unixActual);
    }

    @Test
    public void testRandomTimes() throws Exception {
        TimeZoneOffset moscow = new TimeZoneOffset(4 * 60);
        for (int i = 0; i < 300; i++) {
            UnixTime unix1 = new UnixTime((int)(Math.random() * 1000000000) - 500000000);
            HumanTime human = Times.getHumanTime(unix1, moscow);
            UnixTime unix2 = Times.getUnixTime(human);
            assertEquals(unix1, unix2);
        }
    }
}
