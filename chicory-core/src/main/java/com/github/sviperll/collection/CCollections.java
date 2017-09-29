/*
 * Copyright (c) 2016, Victor Nazarov &lt;asviraspossible@gmail.com&gt;
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

import com.github.sviperll.collection.snapshotable.ImmutableCollections;
import com.github.sviperll.collection.snapshotable.Snapshot;
import com.github.sviperll.collection.snapshotable.SnapshotableList;
import com.github.sviperll.collection.snapshotable.SnapshotableMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class CCollections {
    public static <T> List<T> newArrayList() {
        return new SnapshotableList<>(ListFactories.arrayList());
    }

    public static <T> List<T> newArrayList(Collection<? extends T> c) {
        return new SnapshotableList<>(ListFactories.arrayList(), c);
    }

    public static <K, V> Map<K, V> newHashMap() {
        return new SnapshotableMap<>(MapFactories.hashMap());
    }

    public static <K, V> Map<K, V> newHashMap(Map<? extends K, ? extends V> m) {
        return new SnapshotableMap<>(MapFactories.hashMap(), m);
    }

    public static <K extends Comparable<? super K>, V> Map<K, V> newTreeMap() {
        return new SnapshotableMap<>(MapFactories.<K, V>treeMap());
    }

    public static <K extends Comparable<? super K>, V> Map<K, V> newTreeMap(Map<? extends K, ? extends V> m) {
        return new SnapshotableMap<>(MapFactories.treeMap(), m);
    }

    public static <E> List<? extends E> listOf() {
        return ImmutableCollections.listOf();
    }

    public static <E> List<? extends E> listOf(E e1) {
        return ImmutableCollections.listOf(e1);
    }

    public static <E> List<? extends E> listOf(E e1, E e2) {
        return ImmutableCollections.listOf(e1, e2);
    }

    public static <E> List<? extends E> listOf(E e1, E e2, E e3) {
        return ImmutableCollections.listOf(e1, e2, e3);
    }

    public static <E> List<? extends E> listOf(E e1, E e2, E e3, E e4) {
        return ImmutableCollections.listOf(e1, e2, e3, e4);
    }

    public static <E> List<? extends E> listOf(E e1, E e2, E e3, E e4, E e5) {
        return ImmutableCollections.listOf(e1, e2, e3, e4, e5);
    }

    public static <T> List<? extends T> unmodifiableListSnapshotOf(List<? extends T> argument) {
        return Snapshot.unmodifiableListSnapshotOf(argument);
    }

    public static <K, V> Map<? extends K, ? extends V> unmodifiableMapSnapshotOf(Map<? extends K, ? extends V> argument) {
        return Snapshot.unmodifiableMapSnapshotOf(argument);
    }

    public static <T> Set<? extends T> unmodifiableSetSnapshotOf(Set<? extends T> argument) {
        return Snapshot.unmodifiableSetSnapshotOf(argument);
    }

    public static <T> Collection<? extends T> unmodifiableCollectionSnapshotOf(Collection<? extends T> argument) {
        return Snapshot.unmodifiableCollectionSnapshotOf(argument);
    }

    private CCollections() {
    }
}
