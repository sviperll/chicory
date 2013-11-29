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

import com.github.sviperll.Objects;

public class ClockTime implements Comparable<ClockTime> {
    private final int hour;
    private final int minute;
    private final int second;
    private final int millis;

    public ClockTime(int hour, int minute, int second, int millis) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.millis = millis;
    }

    public int hour() {
        return hour;
    }

    public int minute() {
        return minute;
    }

    public int second() {
        return second;
    }

    public int millis() {
        return millis;
    }

    @Override
    public String toString() {
        return Objects.toString("ClockTime", hour, minute, second, millis);
    }

    @Override
    public int compareTo(ClockTime that) {
        int result = this.hour - that.hour;
        if (result != 0)
            return result;
        result = this.minute - that.minute;
        if (result != 0)
            return result;
        result = this.second - that.second;
        if (result != 0)
            return result;
        result = this.millis - that.millis;
        return result;
    }

    @Override
    public boolean equals(Object thatClockTime) {
        if (this == thatClockTime)
            return true;
        else if(!(thatClockTime instanceof ClockTime))
            throw new IllegalArgumentException("is not ClockTime: " + thatClockTime);
        else {
            ClockTime that = (ClockTime)thatClockTime;
            return this.compareTo(that) == 0;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.hour;
        hash = 67 * hash + this.minute;
        hash = 67 * hash + this.second;
        hash = 67 * hash + this.millis;
        return hash;
    }
}
