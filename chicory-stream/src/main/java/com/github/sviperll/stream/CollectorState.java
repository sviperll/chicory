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

import com.github.sviperll.Applicable;
import com.github.sviperll.BiApplicable;
import com.github.sviperll.BinaryOperatorDefinition;
import com.github.sviperll.Evaluatable;
import com.github.sviperll.OptionalVisitor;
import com.github.sviperll.Supplier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class CollectorState<T, R, E extends Exception> implements Collecting<T, R, E> {
    public static <T, U, R, E extends Exception> CollectorState<T, R, E> optional(final Applicable<? super T, ? extends Collecting<? super T, U, E>> notOptionalCollector, final OptionalVisitor<? super U, R, E> visitor) {
        StatefulCollecting<T, R, E> state = new StatefulCollecting<T, R, E>(null);
        NoResultCollecting<T, U, R, E> noResultCollectorState = new NoResultCollecting<T, U, R, E>(visitor, state, notOptionalCollector);
        state.setBehaviour(noResultCollectorState);
        return new CollectorState<T, R, E>(state, state);
    }

    public static <T, U> CollectorState<T, U, RuntimeException> reducing(final U seed, final BiApplicable<U, ? super T, U> function) {
        ReducingCollecting<T, U, RuntimeException> state = new ReducingCollecting<T, U, RuntimeException>(seed, function);
        return new CollectorState<T, U, RuntimeException>(state, state);
    }

    public static <T, R, E extends Exception> CollectorState<T, R, E> reducing(final BinaryOperatorDefinition<T> operator, final OptionalVisitor<? super T, R, E> visitor) {
        Applicable<T, Collecting<? super T, T, E>> stateFactory = new Applicable<T, Collecting<? super T, T, E>>() {
            @Override
            public Collecting<T, T, E> apply(T seed) {
                return new ReducingCollecting<T, T, E>(seed, operator);
            }
        };
        return optional(stateFactory, visitor);
    }

    public static <T> CollectorState<T, List<T>, RuntimeException> toList() {
        return CollectorState.<T, List<T>>toCollection(new ArrayList<T>());
    }

    public static <T extends Comparable<? super T>> CollectorState<T, List<T>, RuntimeException> toSortedList() {
        Collecting<T, List<T>, RuntimeException> state = new Collecting<T, List<T>, RuntimeException>() {
            private ArrayList<T> list = new ArrayList<T>();
            @Override
            public List<T> get() throws RuntimeException {
                java.util.Collections.sort(list);
                return list;
            }

            @Override
            public void accept(T value) {
                list.add(value);
            }

            @Override
            public boolean needsMore() {
                return true;
            }
        };
        return new CollectorState<T, List<T>, RuntimeException>(state, state);
    }

    public static <T extends Comparable<? super T>> CollectorState<T, List<T>, RuntimeException> toSortedList(final int limit) {
        Collecting<T, List<T>, RuntimeException> state = new Collecting<T, List<T>, RuntimeException>() {
            private TreeSet<T> set = new TreeSet<T>();
            @Override
            public List<T> get() throws RuntimeException {
                ArrayList<T> list = new ArrayList<T>();
                list.addAll(set);
                return list;
            }

            @Override
            public void accept(T value) {
                set.add(value);
                while (set.size() > limit)
                    set.remove(set.last());
            }

            @Override
            public boolean needsMore() {
                return true;
            }
        };
        return new CollectorState<T, List<T>, RuntimeException>(state, state);
    }

    public static <T> CollectorState<T, HashSet<T>, RuntimeException> toHashSet() {
        return toCollection(new HashSet<T>());
    }

    public static <T> CollectorState<T, TreeSet<T>, RuntimeException> toTreeSet() {
        return toCollection(new TreeSet<T>());
    }

    public static <T, R, E extends Exception> CollectorState<T, R, E> findFirst(OptionalVisitor<? super T, R, E> visitor) {
        Applicable<T, Collecting<? super T, T, E>> stateFactory = new Applicable<T, Collecting<? super T, T, E>>() {
            @Override
            public Collecting<T, T, E> apply(final T firstElement) {
                return new Collecting<T, T, E>() {
                    @Override
                    public T get() throws E {
                        return firstElement;
                    }

                    @Override
                    public void accept(T value) {
                    }

                    @Override
                    public boolean needsMore() {
                        return false;
                    }
                };
            }
        };
        return optional(stateFactory, visitor);
    }

    public static <T, R extends Collection<T>> CollectorState<T, R, RuntimeException> toCollection(final R collection) {
        Collecting<T, R, RuntimeException> state = new Collecting<T, R, RuntimeException>() {
            @Override
            public R get() throws RuntimeException {
                return collection;
            }

            @Override
            public void accept(T value) {
                collection.add(value);
            }

            @Override
            public boolean needsMore() {
                return true;
            }
        };
        return new CollectorState<T, R, RuntimeException>(state, state);
    }

    public static <I, K, V, R extends Map<K, V>> CollectorState<I, R, RuntimeException> toMap(final Applicable<? super I, ? extends K> function, final Supplier<? extends Collecting<I, V, ? extends RuntimeException>> collector, final Supplier<R> factory) {
        Collecting<I, R, RuntimeException> state = new Collecting<I, R, RuntimeException>() {
            HashMap<K, Collecting<I, V, ? extends RuntimeException>> state = new HashMap<K, Collecting<I, V, ? extends RuntimeException>>();
            @Override
            public R get() throws RuntimeException {
                R result = factory.get();
                for (Map.Entry<K, Collecting<I, V, ? extends RuntimeException>> entry: state.entrySet()) {
                    result.put(entry.getKey(), entry.getValue().get());
                }
                return result;
            }

            @Override
            public void accept(I value) {
                K key = function.apply(value);
                Collecting<I, V, ? extends RuntimeException> s = state.get(key);
                if (s == null) {
                    s = collector.get();
                    state.put(key, s);
                }
                s.accept(value);
            }

            @Override
            public boolean needsMore() {
                return true;
            }
        };
        return new CollectorState<I, R, RuntimeException>(state, state);
    }

    public static <I, K, V> CollectorState<I, HashMap<K, V>, RuntimeException> toHashMap(final Applicable<? super I, ? extends K> function, final Supplier<? extends Collecting<I, V, ? extends RuntimeException>> collector) {
        return toMap(function, collector, new Supplier<HashMap<K, V>>() {
            @Override
            public HashMap<K, V> get() {
                return new HashMap<K, V>();
            }
        });
    }

    public static <I, K, V> CollectorState<I, TreeMap<K, V>, RuntimeException> toTreeMap(final Applicable<? super I, ? extends K> function, final Supplier<? extends Collecting<I, V, ? extends RuntimeException>> collector) {
        return toMap(function, collector, new Supplier<TreeMap<K, V>>() {
            @Override
            public TreeMap<K, V> get() {
                return new TreeMap<K, V>();
            }
        });
    }

    public static <T> CollectorState<T, Integer, RuntimeException> counting() {
        Collecting<T, Integer, RuntimeException> state = new Collecting<T, Integer, RuntimeException>() {
            private int count = 0;

            @Override
            public void accept(T value) {
                count++;
            }

            @Override
            public boolean needsMore() {
                return true;
            }

            @Override
            public Integer get() throws RuntimeException {
                return count;
            }
        };
        return new CollectorState<T, Integer, RuntimeException>(state, state);
    }

    public static CollectorState<Integer, Integer, RuntimeException> summingInt() {
        Collecting<Integer, Integer, RuntimeException> state = new Collecting<Integer, Integer, RuntimeException>() {
            private int sum = 0;

            @Override
            public void accept(Integer value) {
                sum += value;
            }

            @Override
            public boolean needsMore() {
                return true;
            }

            @Override
            public Integer get() throws RuntimeException {
                return sum;
            }
        };
        return new CollectorState<Integer, Integer, RuntimeException>(state, state);
    }

    public static CollectorState<Long, Long, RuntimeException> summingLong() {
        Collecting<Long, Long, RuntimeException> state = new Collecting<Long, Long, RuntimeException>() {
            private long sum = 0;

            @Override
            public void accept(Long value) {
                sum += value;
            }

            @Override
            public boolean needsMore() {
                return true;
            }

            @Override
            public Long get() throws RuntimeException {
                return sum;
            }
        };
        return new CollectorState<Long, Long, RuntimeException>(state, state);
    }

    public static CollectorState<Double, Double, RuntimeException> summingDouble() {
        Collecting<Double, Double, RuntimeException> state = new Collecting<Double, Double, RuntimeException>() {
            private double sum = 0.0;

            @Override
            public void accept(Double value) {
                sum += value;
            }

            @Override
            public boolean needsMore() {
                return true;
            }

            @Override
            public Double get() throws RuntimeException {
                return sum;
            }
        };
        return new CollectorState<Double, Double, RuntimeException>(state, state);
    }

    public static CollectorState<Integer, Integer, RuntimeException> productingInt() {
        Collecting<Integer, Integer, RuntimeException> state = new Collecting<Integer, Integer, RuntimeException>() {
            private Integer product = 1;

            @Override
            public void accept(Integer value) {
                product *= value;
            }

            @Override
            public boolean needsMore() {
                return true;
            }

            @Override
            public Integer get() throws RuntimeException {
                return product;
            }
        };
        return new CollectorState<Integer, Integer, RuntimeException>(state, state);
    }

    public static CollectorState<Long, Long, RuntimeException> productingLong() {
        Collecting<Long, Long, RuntimeException> state = new Collecting<Long, Long, RuntimeException>() {
            private long product = 1;

            @Override
            public void accept(Long value) {
                product *= value;
            }

            @Override
            public boolean needsMore() {
                return true;
            }

            @Override
            public Long get() throws RuntimeException {
                return product;
            }
        };
        return new CollectorState<Long, Long, RuntimeException>(state, state);
    }

    public static CollectorState<Double, Double, RuntimeException> productingDouble() {
        Collecting<Double, Double, RuntimeException> state = new Collecting<Double, Double, RuntimeException>() {
            private double product = 1;

            @Override
            public void accept(Double value) {
                product *= value;
            }

            @Override
            public boolean needsMore() {
                return true;
            }

            @Override
            public Double get() throws RuntimeException {
                return product;
            }
        };
        return new CollectorState<Double, Double, RuntimeException>(state, state);
    }

    public static <T extends Comparable<? super T>, R, E extends Exception> CollectorState<T, R, E> maximum(OptionalVisitor<? super T, R, E> visitor) {
        Applicable<T, Collecting<? super T, T, E>> stateFactory = new Applicable<T, Collecting<? super T, T, E>>() {
            @Override
            public Collecting<T, T, E> apply(final T firstElement) {
                return new Collecting<T, T, E>() {
                    private T maximum = firstElement;
                    @Override
                    public T get() throws E {
                        return maximum;
                    }

                    @Override
                    public void accept(T value) {
                        if (value.compareTo(maximum) > 0)
                            maximum = value;
                    }

                    @Override
                    public boolean needsMore() {
                        return true;
                    }
                };
            }
        };
        return optional(stateFactory, visitor);
    }

    public static <T, R, E extends Exception> CollectorState<T, R, E> maximum(final Comparator<? super T> comparator, OptionalVisitor<? super T, R, E> visitor) {
        Applicable<T, Collecting<? super T, T, E>> stateFactory = new Applicable<T, Collecting<? super T, T, E>>() {
            @Override
            public Collecting<T, T, E> apply(final T firstElement) {
                return new Collecting<T, T, E>() {
                    private T maximum = firstElement;
                    @Override
                    public T get() throws E {
                        return maximum;
                    }

                    @Override
                    public void accept(T value) {
                        if (comparator.compare(value, maximum) > 0)
                            maximum = value;
                    }

                    @Override
                    public boolean needsMore() {
                        return true;
                    }
                };
            }
        };
        return optional(stateFactory, visitor);
    }

    public static <T extends Comparable<? super T>, R, E extends Exception> CollectorState<T, R, E> minimum(OptionalVisitor<? super T, R, E> visitor) {
        Applicable<T, Collecting<? super T, T, E>> stateFactory = new Applicable<T, Collecting<? super T, T, E>>() {
            @Override
            public Collecting<T, T, E> apply(final T firstElement) {
                return new Collecting<T, T, E>() {
                    private T minimum = firstElement;
                    @Override
                    public T get() throws E {
                        return minimum;
                    }

                    @Override
                    public void accept(T value) {
                        if (value.compareTo(minimum) < 0)
                            minimum = value;
                    }

                    @Override
                    public boolean needsMore() {
                        return true;
                    }
                };
            }
        };
        return optional(stateFactory, visitor);
    }

    public static <T, R, E extends Exception> CollectorState<T, R, E> minimum(final Comparator<? super T> comparator, OptionalVisitor<? super T, R, E> visitor) {
        Applicable<T, Collecting<? super T, T, E>> stateFactory = new Applicable<T, Collecting<? super T, T, E>>() {
            @Override
            public Collecting<T, T, E> apply(final T firstElement) {
                return new Collecting<T, T, E>() {
                    private T minimum = firstElement;
                    @Override
                    public T get() throws E {
                        return minimum;
                    }

                    @Override
                    public void accept(T value) {
                        if (comparator.compare(value, minimum) < 0)
                            minimum = value;
                    }

                    @Override
                    public boolean needsMore() {
                        return true;
                    }
                };
            }
        };
        return optional(stateFactory, visitor);
    }

    public static <R, E extends Exception> CollectorState<String, R, E> joiningStrings(final String separator, final OptionalVisitor<String, R, E> visitor) {
        Applicable<String, Collecting<String, String, E>> stateFactory = new Applicable<String, Collecting<String, String, E>>() {
            @Override
            public Collecting<String, String, E> apply(final String firstElement) {
                return new Collecting<String, String, E>() {
                    private StringBuilder builder = new StringBuilder(firstElement);
                    @Override
                    public String get() {
                        return builder.toString();
                    }

                    @Override
                    public void accept(String value) {
                        builder.append(separator);
                        builder.append(value);
                    }

                    @Override
                    public boolean needsMore() {
                        return true;
                    }
                };
            }
        };
        return optional(stateFactory, visitor);
    }

    public static CollectorState<String, String, RuntimeException> joiningStrings() {
        Collecting<String, String, RuntimeException> collecting = new Collecting<String, String, RuntimeException>() {
            private StringBuilder builder = new StringBuilder();
            @Override
            public String get() {
                return builder.toString();
            }

            @Override
            public void accept(String value) {
                builder.append(value);
            }

            @Override
            public boolean needsMore() {
                return true;
            }
        };
        return new CollectorState<String, String, RuntimeException>(collecting, collecting);
    }

    public static <T, R, E extends Exception> CollectorState<T, R, E> of(Collecting<T, R, E> collecting) {
        if (collecting instanceof CollectorState)
            return (CollectorState<T, R, E>)collecting;
        else
            return new CollectorState<T, R, E>(collecting, collecting);
    }

    private final SaturableConsuming<T> consumer;
    private final ThrowingSupplier<R, E> supplier;
    public CollectorState(SaturableConsuming<T> consumer, ThrowingSupplier<R, E> supplier) {
        this.consumer = consumer;
        this.supplier = supplier;
    }

    @Override
    public void accept(T value) {
        consumer.accept(value);
    }

    @Override
    public boolean needsMore() {
        return consumer.needsMore();
    }

    @Override
    public R get() throws E {
        return supplier.get();
    }

    public CollectorState<T, R, E> limiting(final int limit) {
        return new CollectorState<T, R, E>(SaturableConsumer.of(consumer).limiting(limit), supplier);
    }

    public CollectorState<T, R, E> skipping(final int offset) {
        return new CollectorState<T, R, E>(SaturableConsumer.of(consumer).skipping(offset), supplier);
    }

    public CollectorState<T, R, E> filtering(final Evaluatable<? super T> predicate) {
        return new CollectorState<T, R, E>(SaturableConsumer.of(consumer).filtering(predicate), supplier);
    }

    public <U> CollectorState<U, R, E> mapping(final Applicable<U, ? extends T> function) {
        return new CollectorState<U, R, E>(SaturableConsumer.of(consumer).mapping(function), supplier);
    }

    public <U> CollectorState<T, U, E> finallyTransforming(final Applicable<? super R, U> function) {
        ThrowingSupplier<U, E> newSupplier = new ThrowingSupplier<U, E>() {
            @Override
            public U get() throws E {
                return function.apply(supplier.get());
            }
        };
        return new CollectorState<T, U, E>(consumer, newSupplier);
    }

    private static class StatefulCollecting<T, R, E extends Exception> implements Collecting<T, R, E> {
        private Collecting<T, R, E> behaviour;

        public StatefulCollecting(Collecting<T, R, E> behaviour) {
            this.behaviour = behaviour;
        }

        public void setBehaviour(Collecting<T, R, E> behaviour) {
            this.behaviour = behaviour;
        }

        @Override
        public void accept(T value) {
            behaviour.accept(value);
        }

        @Override
        public R get() throws E {
            return behaviour.get();
        }

        @Override
        public boolean needsMore() {
            return behaviour.needsMore();
        }
    }


    private static class NoResultCollecting<T, U, R, E extends Exception> implements Collecting<T, R, E> {
        private final OptionalVisitor<? super U, R, E> visitor;
        private final StatefulCollecting<T, R, E> state;
        private final Applicable<? super T, ? extends Collecting<? super T, U, E>> collector;
        public NoResultCollecting(OptionalVisitor<? super U, R, E> visitor, StatefulCollecting<T, R, E> state, Applicable<? super T, ? extends Collecting<? super T, U, E>> collector) {
            this.visitor = visitor;
            this.state = state;
            this.collector = collector;
        }

        @Override
        public R get() throws E {
            return visitor.missing();
        }

        @Override
        public void accept(T value) {
            Collecting<? super T, U, E> reducingConsumer = collector.apply(value);
            state.setBehaviour(new NotOptionalCollectorState<T, U, R, E>(reducingConsumer, visitor));
        }

        @Override
        public boolean needsMore() {
            return true;
        }
    }

    private static class ReducingCollecting<T, U, E extends Exception> implements Collecting<T, U, E> {
        private final BiApplicable<U, ? super T, U> function;
        private U result;

        public ReducingCollecting(U seed,
                                BiApplicable<U, ? super T, U> function) {
            this.result = seed;
            this.function = function;
        }

        @Override
        public void accept(T value) {
            result = function.apply(result, value);
        }

        @Override
        public U get() {
            return result;
        }

        @Override
        public boolean needsMore() {
            return true;
        }
    }

    private static class NotOptionalCollectorState<T, U, R, E extends Exception> implements Collecting<T, R, E> {
        private final Collecting<? super T, U, E> consumer;
        private final OptionalVisitor<? super U, R, E> visitor;

        public NotOptionalCollectorState(Collecting<? super T, U, E> consumer, OptionalVisitor<? super U, R, E> visitor) {
            this.consumer = consumer;
            this.visitor = visitor;
        }

        @Override
        public R get() throws E {
            return visitor.present(consumer.get());
        }

        @Override
        public void accept(T value) {
            consumer.accept(value);
        }

        @Override
        public boolean needsMore() {
            return consumer.needsMore();
        }
    }
}
