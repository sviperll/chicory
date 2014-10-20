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

import java.util.Arrays;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author vir
 */
public class ByteArrayTest {
    @Test
    public void testEqualsForEqualArrays() {
        byte[] a = new byte[] {1, 2, 3};
        byte[] b = new byte[] {1, 2, 3};
        assertEquals(new ByteArray(a).equals(new ByteArray(b)), Arrays.equals(a, b));
    }

    @Test
    public void testEqualsForNotEqualArrays() {
        byte[] a = new byte[] {1, 2, 3};
        byte[] b = new byte[] {2, 3, 1};
        assertEquals(new ByteArray(a).equals(new ByteArray(b)), Arrays.equals(a, b));
    }


    @Test
    public void testEqualsForFirstArrayShorter() {
        byte[] a = new byte[] {1, 2};
        byte[] b = new byte[] {1, 2, 3};
        assertEquals(new ByteArray(a).equals(new ByteArray(b)), Arrays.equals(a, b));
    }

    @Test
    public void testEqualsForSecondArrayShorter() {
        byte[] a = new byte[] {1, 2, 3};
        byte[] b = new byte[] {1, 2};
        assertEquals(new ByteArray(a).equals(new ByteArray(b)), Arrays.equals(a, b));
    }

    @Test
    public void testEqualsForEmptyArrays() {
        byte[] a = new byte[] {};
        byte[] b = new byte[] {};
        assertEquals(new ByteArray(a).equals(new ByteArray(b)), Arrays.equals(a, b));
    }

    @Test
    public void testCompareToForEqualArrays() {
        byte[] a = new byte[] {1, 2, 3};
        byte[] b = new byte[] {1, 2, 3};
        assertTrue(new ByteArray(a).compareTo(new ByteArray(b)) == 0);
    }

    @Test
    public void testCompareToForNotEqualArrays() {
        byte[] a = new byte[] {1, 2, 3};
        byte[] b = new byte[] {2, 3, 1};
        assertTrue(new ByteArray(a).compareTo(new ByteArray(b)) < 0);
    }


    @Test
    public void testCompareToForFirstArrayShorter() {
        byte[] a = new byte[] {1, 2};
        byte[] b = new byte[] {1, 2, 3};
        assertTrue(new ByteArray(a).compareTo(new ByteArray(b)) < 0);
    }

    @Test
    public void testCompareToForSecondArrayShorter() {
        byte[] a = new byte[] {1, 2, 3};
        byte[] b = new byte[] {1, 2};
        assertTrue(new ByteArray(a).compareTo(new ByteArray(b)) > 0);
    }

    @Test
    public void testCompareToForEmptyArrays() {
        byte[] a = new byte[] {};
        byte[] b = new byte[] {};
        assertTrue(new ByteArray(a).compareTo(new ByteArray(b)) == 0);
    }
}
