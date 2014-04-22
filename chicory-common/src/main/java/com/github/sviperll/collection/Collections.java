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
package com.github.sviperll.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Collections {
    /** Created new mobifieble TreeMap instance */
    public static <K, V> Map<K, V> newTreeMap() {
        return new TreeMap<>();
    }

    /** Created new mobifieble HashMap instance */
    public static <K, V> Map<K, V> newHashMap() {
        return new HashMap<>();
    }

    /** Created new mobifieble TreeSet instance */
    public static <E> Set<E> newTreeSet() {
        return new TreeSet<>();
    }

    /** Created new mobifieble HashSet instance */
    public static <E> Set<E> newHashSet() {
        return new HashSet<>();
    }

    /** Created new mobifieble ArrayList instance */
    public static <E> List<E> newArrayList() {
        return new ArrayList<>();
    }

    /** Created new mobifieble ArrayList instance of given capacity */
    public static <E> List<E> newArrayList(int capacity) {
        return new ArrayList<>(capacity);
    }

    /** Created new mobifieble ArrayList instance with one given element */
    public static <E> List<E> newArrayListOf(E e1) {
        List<E> elist = newArrayList(1);
        elist.add(e1);
        return elist;
    }

    /** Created new mobifieble ArrayList instance with two given elements */
    public static <E> List<E> newArrayListOf(E e1, E e2) {
        List<E> elist = newArrayList(2);
        elist.add(e1);
        elist.add(e2);
        return elist;
    }

    /** Created new mobifieble ArrayList instance with three given elements */
    public static <E> List<E> newArrayListOf(E e1, E e2, E e3) {
        List<E> elist = newArrayList(3);
        elist.add(e1);
        elist.add(e2);
        elist.add(e3);
        return elist;
    }

    private Collections() {}
}
