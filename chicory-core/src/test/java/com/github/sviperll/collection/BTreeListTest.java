/*
 * Copyright (c) 2015, vir
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation and/or
 *     other materials provided with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.sviperll.collection;

import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author vir
 */
public class BTreeListTest {

    public BTreeListTest() {
    }

    @Test
    public void testGet() {
        BTreeList<String> instance = new BTreeList<String>(2);
        instance.addAll(Arrays.asList("a", "b", "c", "d"));
        assertThat(instance.size(), equalTo(4));
        assertThat(instance.get(0), equalTo("a"));
        assertThat(instance.get(1), equalTo("b"));
        assertThat(instance.get(2), equalTo("c"));
        assertThat(instance.get(3), equalTo("d"));
        try {
            instance.get(-1);
            assertTrue(false);
        } catch (IndexOutOfBoundsException ex) {
            assertTrue(true);
            assertThat(ex.getMessage(), equalTo("-1"));
        }
        try {
            instance.get(-2);
            assertTrue(false);
        } catch (IndexOutOfBoundsException ex) {
            assertTrue(true);
            assertThat(ex.getMessage(), equalTo("-2"));
        }
        try {
            instance.get(-100);
            assertTrue(false);
        } catch (IndexOutOfBoundsException ex) {
            assertTrue(true);
            assertThat(ex.getMessage(), equalTo("-100"));
        }
        try {
            instance.get(4);
            assertTrue(false);
        } catch (IndexOutOfBoundsException ex) {
            assertTrue(true);
            assertThat(ex.getMessage(), equalTo("4"));
        }
        try {
            instance.get(5);
            assertTrue(false);
        } catch (IndexOutOfBoundsException ex) {
            assertTrue(true);
            assertThat(ex.getMessage(), equalTo("5"));
        }
        try {
            instance.get(200);
            assertTrue(false);
        } catch (IndexOutOfBoundsException ex) {
            assertTrue(true);
            assertThat(ex.getMessage(), equalTo("200"));
        }
    }

    /**
     * Test of add method, of class BTreeList.
     */
    @Test
    public void testAdd() {
        BTreeList<String> instance = new BTreeList<String>(2);
        instance.addAll(Arrays.asList("a", "b", "c", "d"));
        assertThat(instance.size(), equalTo(4));
        assertThat(instance.get(0), equalTo("a"));
        assertThat(instance.get(1), equalTo("b"));
        assertThat(instance.get(2), equalTo("c"));
        assertThat(instance.get(3), equalTo("d"));
        instance.add(2, "x");
        assertThat(instance.size(), equalTo(5));
        assertThat(instance.get(0), equalTo("a"));
        assertThat(instance.get(1), equalTo("b"));
        assertThat(instance.get(2), equalTo("x"));
        assertThat(instance.get(3), equalTo("c"));
        assertThat(instance.get(4), equalTo("d"));
        instance.add(0, "y");
        assertThat(instance.size(), equalTo(6));
        assertThat(instance.get(0), equalTo("y"));
        assertThat(instance.get(1), equalTo("a"));
        assertThat(instance.get(2), equalTo("b"));
        assertThat(instance.get(3), equalTo("x"));
        assertThat(instance.get(4), equalTo("c"));
        assertThat(instance.get(5), equalTo("d"));
        instance.add(6, "z");
        assertThat(instance.size(), equalTo(7));
        assertThat(instance.get(0), equalTo("y"));
        assertThat(instance.get(1), equalTo("a"));
        assertThat(instance.get(2), equalTo("b"));
        assertThat(instance.get(3), equalTo("x"));
        assertThat(instance.get(4), equalTo("c"));
        assertThat(instance.get(5), equalTo("d"));
        assertThat(instance.get(6), equalTo("z"));
        try {
            instance.add(-1, "w");
            assertTrue(false);
        } catch (IndexOutOfBoundsException ex) {
            assertTrue(true);
            assertThat(ex.getMessage(), equalTo("-1"));
        }
        try {
            instance.add(-2, "w");
            assertTrue(false);
        } catch (IndexOutOfBoundsException ex) {
            assertTrue(true);
            assertThat(ex.getMessage(), equalTo("-2"));
        }
        try {
            instance.add(-100, "w");
            assertTrue(false);
        } catch (IndexOutOfBoundsException ex) {
            assertTrue(true);
            assertThat(ex.getMessage(), equalTo("-100"));
        }
        try {
            instance.add(8, "w");
            assertTrue(false);
        } catch (IndexOutOfBoundsException ex) {
            assertTrue(true);
            assertThat(ex.getMessage(), equalTo("8"));
        }
        try {
            instance.add(9, "w");
            assertTrue(false);
        } catch (IndexOutOfBoundsException ex) {
            assertTrue(true);
            assertThat(ex.getMessage(), equalTo("9"));
        }
        try {
            instance.add(100, "w");
            assertTrue(false);
        } catch (IndexOutOfBoundsException ex) {
            assertTrue(true);
            assertThat(ex.getMessage(), equalTo("100"));
        }
    }


    /**
     * Test of add method, of class BTreeList.
     */
    @Test
    public void testRemove() {
        BTreeList<String> instance = new BTreeList<String>(2);
        instance.addAll(Arrays.asList("a", "b", "c", "d", "e", "f"));
        assertThat(instance.size(), equalTo(6));
        assertThat(instance.get(0), equalTo("a"));
        assertThat(instance.get(1), equalTo("b"));
        assertThat(instance.get(2), equalTo("c"));
        assertThat(instance.get(3), equalTo("d"));
        assertThat(instance.get(4), equalTo("e"));
        assertThat(instance.get(5), equalTo("f"));
        instance.remove(0);
        assertThat(instance.size(), equalTo(5));
        assertThat(instance.get(0), equalTo("b"));
        assertThat(instance.get(1), equalTo("c"));
        assertThat(instance.get(2), equalTo("d"));
        assertThat(instance.get(3), equalTo("e"));
        assertThat(instance.get(4), equalTo("f"));
        instance.remove(2);
        assertThat(instance.size(), equalTo(4));
        assertThat(instance.get(0), equalTo("b"));
        assertThat(instance.get(1), equalTo("c"));
        assertThat(instance.get(2), equalTo("e"));
        assertThat(instance.get(3), equalTo("f"));
        instance.remove(3);
        assertThat(instance.size(), equalTo(3));
        assertThat(instance.get(0), equalTo("b"));
        assertThat(instance.get(1), equalTo("c"));
        assertThat(instance.get(2), equalTo("e"));
    }

    /**
     * Test of add method, of class BTreeList.
     */
    @Test
    public void testRemoveValue() {
        BTreeList<String> instance = new BTreeList<String>(2);
        instance.addAll(Arrays.asList("a", "b", "a", "d", "a", "f"));
        assertThat(instance.size(), equalTo(6));
        assertThat(instance.get(0), equalTo("a"));
        assertThat(instance.get(1), equalTo("b"));
        assertThat(instance.get(2), equalTo("a"));
        assertThat(instance.get(3), equalTo("d"));
        assertThat(instance.get(4), equalTo("a"));
        assertThat(instance.get(5), equalTo("f"));
        instance.remove("a");
        assertThat(instance.size(), equalTo(3));
        assertThat(instance.get(0), equalTo("b"));
        assertThat(instance.get(1), equalTo("d"));
        assertThat(instance.get(2), equalTo("f"));
    }

    @Test
    public void testIterator() {
        BTreeList<String> instance = new BTreeList<String>(2);
        instance.addAll(Arrays.asList("a", "b", "c", "d"));
        ListIterator<String> iterator = instance.listIterator();
        assertThat(iterator.hasPrevious(), is(false));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), equalTo("a"));
        assertThat(iterator.hasPrevious(), is(true));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), equalTo("b"));
        assertThat(iterator.hasPrevious(), is(true));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.previous(), equalTo("b"));
        assertThat(iterator.hasPrevious(), is(true));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.previous(), equalTo("a"));
        assertThat(iterator.hasPrevious(), is(false));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), equalTo("a"));
        assertThat(iterator.hasPrevious(), is(true));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), equalTo("b"));
        assertThat(iterator.hasPrevious(), is(true));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), equalTo("c"));
        assertThat(iterator.hasPrevious(), is(true));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), equalTo("d"));
        assertThat(iterator.hasPrevious(), is(true));
        assertThat(iterator.hasNext(), is(false));
        try {
            iterator.next();
            assertFalse(true);
        } catch (NoSuchElementException ex) {
            assertTrue(true);
        }
    }
}
