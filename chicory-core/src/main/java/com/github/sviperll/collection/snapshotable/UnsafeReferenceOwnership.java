/*
 * Copyright (c) 2017, Victor Nazarov <asviraspossible@gmail.com>
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

package com.github.sviperll.collection.snapshotable;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
class UnsafeReferenceOwnership {
    @OnlyReference
    static <T> List<T> unmodifiableWrapperForListWithNoOtherReferencesAnywhere(@OnlyReference @TakesOwnership List<T> argument) {
        return new UnmodifiableWrapperForListWithNoOtherReferencesAnywhere<>(argument);
    }

    @OnlyReference
    static <T> Set<T> unmodifiableWrapperForSetWithNoOtherReferencesAnywhere(@OnlyReference @TakesOwnership Set<T> argument) {
        return new UnmodifiableWrapperForSetWithNoOtherReferencesAnywhere<>(argument);
    }

    @OnlyReference
    static <T> Collection<T> unmodifiableWrapperForCollectionWithNoOtherReferencesAnywhere(@OnlyReference @TakesOwnership Collection<T> argument) {
        return new UnmodifiableWrapperForCollectionWithNoOtherReferencesAnywhere<>(argument);
    }

    @OnlyReference
    static <K, V> Map<K, V> unmodifiableWrapperForMapWithNoOtherReferencesAnywhere(@OnlyReference @TakesOwnership Map<K, V> argument) {
        return new UnmodifiableWrapperForMapWithNoOtherReferencesAnywhere<>(argument);
    }

    static <T> boolean noModifiableReferencesExistsAnywhere(Collection<T> argument) {
        return argument instanceof UnmodifiableWrapperForCollectionWithNoOtherReferencesAnywhere
                || argument instanceof UnmodifiableWrapperForSetWithNoOtherReferencesAnywhere
                || argument instanceof UnmodifiableWrapperForListWithNoOtherReferencesAnywhere;
    }

    static <K, V> boolean noModifiableReferencesExistsAnywhere(Map<K, V> argument) {
        return argument instanceof UnmodifiableWrapperForMapWithNoOtherReferencesAnywhere;
    }
    
    private UnsafeReferenceOwnership() {
    }

    private static class UnmodifiableWrapperForListWithNoOtherReferencesAnywhere<T> extends AbstractList<T> {
        private final List<T> list;
        private UnmodifiableWrapperForListWithNoOtherReferencesAnywhere(List<T> list) {
            this.list = list;
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public boolean isEmpty() {
            return list.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return list.contains(o);
        }

        @Override
        public Iterator<T> iterator() {
            return list.iterator();
        }

        @Override
        public Object[] toArray() {
            return list.toArray();
        }

        @Override
        @SuppressWarnings("SuspiciousToArrayCall")
        public <T> T[] toArray(T[] a) {
            return list.toArray(a);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return list.containsAll(c);
        }

        @Override
        public T get(int index) {
            return list.get(index);
        }

        @Override
        public int indexOf(Object o) {
            return list.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return list.lastIndexOf(o);
        }

        @Override
        public ListIterator<T> listIterator() {
            return list.listIterator();
        }

        @Override
        public ListIterator<T> listIterator(int index) {
            return list.listIterator();
        }

        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            return unmodifiableWrapperForListWithNoOtherReferencesAnywhere(list.subList(fromIndex, toIndex));
        }
    }

    private static class UnmodifiableWrapperForSetWithNoOtherReferencesAnywhere<T> extends AbstractSet<T> {
        private final Set<T> set;
        private UnmodifiableWrapperForSetWithNoOtherReferencesAnywhere(Set<T> set) {
            this.set = set;
        }

        @Override
        public int size() {
            return set.size();
        }

        @Override
        public boolean isEmpty() {
            return set.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return set.contains(o);
        }

        @Override
        public Iterator<T> iterator() {
            return set.iterator();
        }

        @Override
        public Object[] toArray() {
            return set.toArray();
        }

        @Override
        @SuppressWarnings("SuspiciousToArrayCall")
        public <T> T[] toArray(T[] a) {
            return set.toArray(a);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return set.containsAll(c);
        }
    }

    private static class UnmodifiableWrapperForCollectionWithNoOtherReferencesAnywhere<T> extends AbstractCollection<T> {
        private final Collection<T> set;
        private UnmodifiableWrapperForCollectionWithNoOtherReferencesAnywhere(Collection<T> set) {
            this.set = set;
        }

        @Override
        public int size() {
            return set.size();
        }

        @Override
        public boolean isEmpty() {
            return set.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return set.contains(o);
        }

        @Override
        public Iterator<T> iterator() {
            return set.iterator();
        }

        @Override
        public Object[] toArray() {
            return set.toArray();
        }

        @Override
        @SuppressWarnings("SuspiciousToArrayCall")
        public <T> T[] toArray(T[] a) {
            return set.toArray(a);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return set.containsAll(c);
        }

        @Override
        public void clear() {
            set.clear();
        }
    }

    private static class UnmodifiableWrapperForMapWithNoOtherReferencesAnywhere<K, V> extends AbstractMap<K, V> {
        private final Map<K, V> map;
        private Set<K> keySet = null;
        private Set<Map.Entry<K, V>> entrySet = null;
        private Collection<V> values = null;
        private UnmodifiableWrapperForMapWithNoOtherReferencesAnywhere(Map<K, V> map) {
            this.map = map;
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return map.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        @Override
        public V get(Object key) {
            return map.get(key);
        }

        @Override
        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public Set<K> keySet() {
            if (keySet == null)
                keySet = unmodifiableWrapperForSetWithNoOtherReferencesAnywhere(map.keySet());
            return keySet;
        }

        @Override
        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public Collection<V> values() {
            if (values == null)
                values = unmodifiableWrapperForCollectionWithNoOtherReferencesAnywhere(map.values());
            return values;
        }

        @Override
        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public Set<Map.Entry<K, V>> entrySet() {
            if (entrySet == null)
                entrySet= unmodifiableWrapperForSetWithNoOtherReferencesAnywhere(map.entrySet());
            return entrySet;
        }
    }

}
