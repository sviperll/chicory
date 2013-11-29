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

public class TimeZoneOffset implements Comparable<TimeZoneOffset> {
    private final long minutes;

    public TimeZoneOffset(long minutes) {
        this.minutes = minutes;
    }

    public long minutes() {
        return minutes;
    }

    @Override
    public String toString() {
        return Objects.toString("Offset", minutes);
    }

    @Override
    public int compareTo(TimeZoneOffset that) {
        return (int)(this.minutes - that.minutes);
    }

    @Override
    public boolean equals(Object thatOffset) {
        if (this == thatOffset)
            return true;
        else if(!(thatOffset instanceof TimeZoneOffset))
            throw new IllegalArgumentException("is not Offset: " + thatOffset);
        else {
            TimeZoneOffset that = (TimeZoneOffset)thatOffset;
            return this.compareTo(that) == 0;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (int)(this.minutes ^ (this.minutes >>> 32));
        return hash;
    }

}
