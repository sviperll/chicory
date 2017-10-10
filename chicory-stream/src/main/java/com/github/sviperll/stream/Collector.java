/*
 * Copyright (c) 2014, Victor Nazarov <asviraspossible@gmail.com>
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
package com.github.sviperll.stream;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class Collector<T, R> implements Supplier<Collecting<T, R>> {
    public static <T, R> Collector<T, Optional<R>> optional(
            Function<? super T, Collecting<? super T, R>> notOptionalCollector) {
        return new Collector<>(() -> CollectorState.optional(notOptionalCollector));
    }

    public static <T, U> Collector<T, U> reducing(U seed, BiFunction<U, ? super T, U> function) {
        return new Collector<>(() -> CollectorState.reducing(seed, function));
    }

    public static <T> Collector<T, Optional<T>> reducing(BinaryOperator<T> operator) {
        return new Collector<>(() -> CollectorState.reducing(operator));
    }

    public static Collector<String, Optional<String>> joiningStrings(String separator) {
        return new Collector<>(() -> CollectorState.joiningStrings(separator));
    }

    public static Collector<String, String> joiningStrings() {
        return new Collector<>(() -> CollectorState.joiningStrings());
    }

    public static <T> Collector<T, List<T>> toList() {
        return new Collector<>(() -> CollectorState.toList());
    }

    public static <T extends Comparable<? super T>> Collector<T, List<T>> toSortedList() {
        return new Collector<>(() -> CollectorState.<T>toSortedList());
    }

    /**
     * @param limit limit on the size of resulting list.
     *        If stream contains more than limit elements only first limit elements are returned
     *
     * @return the list of several first minimal elements
     */
    public static <T extends Comparable<? super T>> Collector<T, List<T>> toSortedList(final int limit) {
        return new Collector<>(() -> CollectorState.<T>toSortedList(limit));
    }

    public static <T> Collector<T, HashSet<T>> toHashSet() {
        return new Collector<>(() -> CollectorState.toHashSet());
    }

    public static <T> Collector<T, TreeSet<T>> toTreeSet() {
        return new Collector<>(() -> CollectorState.toTreeSet());
    }

    public static <T> Collector<T, Optional<T>> findFirst() {
        return new Collector<>(() -> CollectorState.findFirst());
    }

    public static <T, R extends Collection<? super T>> Collector<T, R> toCollection(Supplier<R> factory) {
        return new Collector<>(() -> CollectorState.toCollection(factory.get()));
    }

    public static <I, K, V, R extends Map<K, V>> Collector<I, R> toMap(
            Function<? super I, ? extends K> function,
            Supplier<? extends Collecting<I, V>> collector,
            Supplier<R> factory) {
        return new Collector<>(() -> CollectorState.toMap(function, collector, factory));
    }

    public static <I, K, V> Collector<I, HashMap<K, V>> toHashMap(
            Function<? super I, ? extends K> function,
            Supplier<? extends Collecting<I, V>> collector) {
        return new Collector<>(() -> CollectorState.toHashMap(function, collector));
    }

    public static <I, K, V> Collector<I, TreeMap<K, V>> toTreeMap(
            Function<? super I, ? extends K> function,
            Supplier<? extends Collecting<I, V>> collector) {
        return new Collector<>(() -> CollectorState.toTreeMap(function, collector));
    }

    public static <T> Collector<T, Integer> counting() {
        return new Collector<>(() -> CollectorState.counting());
    }

    public static <T extends Comparable<? super T>> Collector<T, Optional<T>> maximum() {
        return new Collector<>(() -> CollectorState.<T>maximum());
    }

    public static <T extends Comparable<? super T>> Collector<T, Optional<T>> minimum() {
        return new Collector<>(() -> CollectorState.<T>minimum());
    }

    public static <T> Collector<T, Optional<T>> maximum(Comparator<? super T> comparator) {
        return new Collector<>(() -> CollectorState.maximum(comparator));
    }

    public static <T> Collector<T, Optional<T>> minimum(Comparator<? super T> comparator) {
        return new Collector<>(() -> CollectorState.minimum(comparator));
    }

    public static Collector<Integer, Integer> summingInt() {
        return new Collector<>(() -> CollectorState.summingInt());
    }

    public static Collector<Long, Long> summingLong() {
        return new Collector<>(() -> CollectorState.summingLong());
    }

    public static Collector<Double, Double> summingDouble() {
        return new Collector<>(() -> CollectorState.summingDouble());
    }

    public static Collector<Integer, Integer> productingInt() {
        return new Collector<>(() -> CollectorState.productingInt());
    }

    public static Collector<Long, Long> productingLong() {
        return new Collector<>(() -> CollectorState.productingLong());
    }

    public static Collector<Double, Double> productingDouble() {
        return new Collector<>(() -> CollectorState.productingDouble());
    }

    private final Supplier<? extends Collecting<T, R>> collector;

    public Collector(Supplier<? extends Collecting<T, R>> collector) {
        this.collector = collector;
    }

    @Override
    public Collecting<T, R> get() {
        return collector.get();
    }

    public Collector<T, R> limiting(int limit) {
        return new Collector<>(() -> CollectorState.of(collector.get()).limiting(limit));
    }

    public Collector<T, R> skipping(int offset) {
        return new Collector<>(() -> CollectorState.of(collector.get()).skipping(offset));
    }

    public Collector<T, R> filtering(Predicate<? super T> predicate) {
        return new Collector<>(() -> CollectorState.of(collector.get()).filtering(predicate));
    }

    public <U> Collector<U, R> mapping(Function<U, ? extends T> function) {
        return new Collector<>(() -> CollectorState.of(collector.get()).mapping(function));
    }

    public <U> Collector<T, U> finallyTransforming(Function<? super R, U> function) {
        return new Collector<>(() -> CollectorState.of(collector.get()).finallyTransforming(function));
    }
}
