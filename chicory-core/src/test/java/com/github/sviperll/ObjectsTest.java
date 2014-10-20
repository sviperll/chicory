/*
 * Copyright (c) 2012, Victor Nazarov <asviraspossible@gmail.com>
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
package com.github.sviperll;

import static org.junit.Assert.*;
import org.junit.Test;

public class ObjectsTest {
    @Test
    public void testToString() {
        assertEquals("new Test(1, \"test\")", Objects.toString("Test", 1, "test"));
    }

    @Test
    public void testToStringWithNulls() {
        assertEquals("new Test(null, \"test\")", Objects.toString("Test", null, "test"));
    }

    @Test
    public void testHashCode() {
        assertTrue(Objects.hashCode(1, 2, 3) != Objects.hashCode(2, 2, 3));
        assertTrue(Objects.hashCode(1, 2, 3) != Objects.hashCode(1, 3, 3));
        assertTrue(Objects.hashCode(1, 2, 3) != Objects.hashCode(1, 2, 4));
    }

    @Test
    public void testHashCodeWithNull() {
        assertTrue(Objects.hashCode(1, 2, 3) != Objects.hashCode(null, 2, 3));
        assertTrue(Objects.hashCode(1, 2, 3) != Objects.hashCode(1, null, 3));
        assertTrue(Objects.hashCode(1, 2, 3) != Objects.hashCode(1, 2, null));
    }

    @Test
    public void testObjectsEqualityWithNulls() {
        assertTrue(Objects.equals(null, null));
    }

    @Test
    public void testObjectsEqualityWithNonNulls() {
        Integer i1 = Integer.valueOf(123);
        Integer i2 = Integer.valueOf(123);
        assertTrue(Objects.equals(i1, i2));
    }

    @Test
    public void testObjectsEqualityWithOneNull() {
        Integer i1 = Integer.valueOf(123);
        assertFalse(Objects.equals(i1, null));
        assertFalse(Objects.equals(null, i1));
    }
}
