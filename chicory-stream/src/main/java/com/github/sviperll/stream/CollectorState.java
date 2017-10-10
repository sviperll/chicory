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

import java.util.ArrayList;
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
class CollectorState<T, R> implements Collecting<T, R> {
    public static <T, U, R> CollectorState<T, Optional<R>> optional(Function<? super T, ? extends Collecting<? super T, R>> notOptionalCollector) {
        StatefulCollecting<T, Optional<R>> state = new StatefulCollecting<>(null);
        NoResultCollecting<T, R> noResultCollectorState = new NoResultCollecting<>(state, notOptionalCollector);
        state.setBehaviour(noResultCollectorState);
        return new CollectorState<>(state, state);
    }

    public static <T, U> CollectorState<T, U> reducing(final U seed, BiFunction<U, ? super T, U> function) {
        ReducingCollecting<T, U> state = new ReducingCollecting<>(seed, function);
        return new CollectorState<>(state, state);
    }

    public static <T> CollectorState<T, Optional<T>> reducing(final BinaryOperator<T> operator) {
        return optional(seed -> new ReducingCollecting<>(seed, operator));
    }

    public static <T> CollectorState<T, List<T>> toList() {
        return CollectorState.<T, List<T>>toCollection(new ArrayList<>());
    }

    public static <T extends Comparable<? super T>> CollectorState<T, List<T>> toSortedList() {
        Collecting<T, List<T>> state = new Collecting<T, List<T>>() {
            private final ArrayList<T> list = new ArrayList<>();
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
        return new CollectorState<>(state, state);
    }

    public static <T extends Comparable<? super T>> CollectorState<T, List<T>> toSortedList(final int limit) {
        Collecting<T, List<T>> state = new Collecting<T, List<T>>() {
            private final TreeSet<T> set = new TreeSet<>();
            @Override
            public List<T> get() {
                ArrayList<T> list = new ArrayList<>();
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
        return new CollectorState<>(state, state);
    }

    public static <T> CollectorState<T, HashSet<T>> toHashSet() {
        return toCollection(new HashSet<>());
    }

    public static <T> CollectorState<T, TreeSet<T>> toTreeSet() {
        return toCollection(new TreeSet<>());
    }

    public static <T> CollectorState<T, Optional<T>> findFirst() {
        return optional(firstElement -> {
            return new Collecting<T, T>() {
                @Override
                public T get() {
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
        });
    }

    public static <T, R extends Collection<? super T>> CollectorState<T, R> toCollection(R collection) {
        Collecting<T, R> state = new Collecting<T, R>() {
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
        return new CollectorState<>(state, state);
    }

    public static <I, K, V, R extends Map<K, V>> CollectorState<I, R> toMap(
            Function<? super I, ? extends K> function,
            Supplier<? extends Collecting<I, V>> collector,
            Supplier<R> factory) {
        Collecting<I, R> state = new Collecting<I, R>() {
            private final HashMap<K, Collecting<I, V>> state = new HashMap<>();
            @Override
            public R get() throws RuntimeException {
                R result = factory.get();
                for (Map.Entry<K, Collecting<I, V>> entry: state.entrySet()) {
                    result.put(entry.getKey(), entry.getValue().get());
                }
                return result;
            }

            @Override
            public void accept(I value) {
                K key = function.apply(value);
                Collecting<I, V> s = state.get(key);
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
        return new CollectorState<>(state, state);
    }

    public static <I, K, V> CollectorState<I, HashMap<K, V>> toHashMap(
            Function<? super I, ? extends K> function,
            Supplier<? extends Collecting<I, V>> collector) {
        return toMap(function, collector, () -> new HashMap<>());
    }

    public static <I, K, V> CollectorState<I, TreeMap<K, V>> toTreeMap(
            Function<? super I, ? extends K> function,
            Supplier<? extends Collecting<I, V>> collector) {
        return toMap(function, collector, () -> new TreeMap<>());
    }

    public static <T> CollectorState<T, Integer> counting() {
        Collecting<T, Integer> state = new Collecting<T, Integer>() {
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
        return new CollectorState<>(state, state);
    }

    public static CollectorState<Integer, Integer> summingInt() {
        return reducing(0, Integer::sum);
    }

    public static CollectorState<Long, Long> summingLong() {
        return reducing(0L, Long::sum);
    }

    public static CollectorState<Double, Double> summingDouble() {
        return reducing(0., Double::sum);
    }

    public static CollectorState<Integer, Integer> productingInt() {
        return reducing(1, (x, y) -> x * y);
    }

    public static CollectorState<Long, Long> productingLong() {
        return reducing(1L, (x, y) -> x * y);
    }

    public static CollectorState<Double, Double> productingDouble() {
        return reducing(1.0, (x, y) -> x * y);
    }

    public static <T extends Comparable<? super T>> CollectorState<T, Optional<T>> maximum() {
        return maximum(Comparator.naturalOrder());
    }

    public static <T> CollectorState<T, Optional<T>> maximum(final Comparator<? super T> comparator) {
        return reducing((x, y) -> comparator.compare(x, y) > 0 ? x : y);
    }

    public static <T extends Comparable<? super T>> CollectorState<T, Optional<T>> minimum() {
        return minimum(Comparator.naturalOrder());
    }

    public static <T> CollectorState<T, Optional<T>> minimum(final Comparator<? super T> comparator) {
        return reducing((x, y) -> comparator.compare(x, y) < 0 ? x : y);
    }

    public static CollectorState<String, Optional<String>> joiningStrings(final String separator) {
        return optional((String firstElement) -> {
                return new Collecting<String, String>() {
                    private final StringBuilder builder = new StringBuilder(firstElement);
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
        });
    }

    public static CollectorState<String, String> joiningStrings() {
        Collecting<String, String> collecting = new Collecting<String, String>() {
            private final StringBuilder builder = new StringBuilder();
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
        return new CollectorState<>(collecting, collecting);
    }

    public static <T, R> CollectorState<T, R> of(Collecting<T, R> collecting) {
        if (collecting instanceof CollectorState)
            return (CollectorState<T, R>)collecting;
        else
            return new CollectorState<>(collecting, collecting);
    }

    private final SaturableConsuming<T> consumer;
    private final Supplier<R> supplier;
    CollectorState(SaturableConsuming<T> consumer, Supplier<R> supplier) {
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
    public R get() {
        return supplier.get();
    }

    public CollectorState<T, R> limiting(final int limit) {
        return new CollectorState<>(SaturableConsumer.of(consumer).limiting(limit), supplier);
    }

    public CollectorState<T, R> skipping(final int offset) {
        return new CollectorState<>(SaturableConsumer.of(consumer).skipping(offset), supplier);
    }

    public CollectorState<T, R> filtering(final Predicate<? super T> predicate) {
        return new CollectorState<>(SaturableConsumer.of(consumer).filtering(predicate), supplier);
    }

    public <U> CollectorState<U, R> mapping(final Function<U, ? extends T> function) {
        return new CollectorState<>(SaturableConsumer.of(consumer).mapping(function), supplier);
    }

    public <U> CollectorState<T, U> finallyTransforming(final Function<? super R, U> function) {
        return new CollectorState<>(consumer, () -> function.apply(supplier.get()));
    }

    private static class StatefulCollecting<T, R> implements Collecting<T, R> {
        private Collecting<T, R> behaviour;

        StatefulCollecting(Collecting<T, R> behaviour) {
            this.behaviour = behaviour;
        }

        public void setBehaviour(Collecting<T, R> behaviour) {
            this.behaviour = behaviour;
        }

        @Override
        public void accept(T value) {
            behaviour.accept(value);
        }

        @Override
        public R get() {
            return behaviour.get();
        }

        @Override
        public boolean needsMore() {
            return behaviour.needsMore();
        }
    }


    private static class NoResultCollecting<T, R> implements Collecting<T, Optional<R>> {
        private final StatefulCollecting<T, Optional<R>> state;
        private final Function<? super T, ? extends Collecting<? super T, R>> collector;
        NoResultCollecting(StatefulCollecting<T, Optional<R>> state, Function<? super T, ? extends Collecting<? super T, R>> collector) {
            this.state = state;
            this.collector = collector;
        }

        @Override
        public Optional<R> get() {
            return Optional.empty();
        }

        @Override
        public void accept(T value) {
            Collecting<? super T, R> reducingConsumer = collector.apply(value);
            state.setBehaviour(new NotOptionalCollectorState<T, R>(reducingConsumer));
        }

        @Override
        public boolean needsMore() {
            return true;
        }
    }

    private static class ReducingCollecting<T, U> implements Collecting<T, U> {
        private final BiFunction<U, ? super T, U> function;
        private U result;

        ReducingCollecting(U seed, BiFunction<U, ? super T, U> function) {
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

    private static class NotOptionalCollectorState<T, R> implements Collecting<T, Optional<R>> {
        private final Collecting<? super T, R> consumer;

        NotOptionalCollectorState(Collecting<? super T, R> consumer) {
            this.consumer = consumer;
        }

        @Override
        public Optional<R> get() {
            return Optional.of(consumer.get());
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
