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
package org.github.sviperll.stream;

import com.github.sviperll.Applicable;
import com.github.sviperll.BiApplicable;
import com.github.sviperll.BinaryOperatorDefinition;
import com.github.sviperll.Evaluatable;
import com.github.sviperll.OptionalVisitor;
import com.github.sviperll.Supplier;
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
public class Collector<T, R, E extends Exception> implements Supplier<Collecting<T, R, E>> {
    public static <T, U, R, E extends Exception> Collector<T, R, E> optional(final Applicable<? super T, Collecting<? super T, U, E>> notOptionalCollector, final OptionalVisitor<? super U, R, E> visitor) {
        return new Collector<T, R, E>(new Supplier<Collecting<T, R, E>>() {
            @Override
            public Collecting<T, R, E> get() {
                return CollectorState.optional(notOptionalCollector, visitor);
            }
        });
    }

    public static <T, U> Collector<T, U, RuntimeException> reducing(final U seed, final BiApplicable<U, ? super T, U> function) {
        return new Collector<T, U, RuntimeException>(new Supplier<Collecting<T, U, RuntimeException>>() {
            @Override
            public Collecting<T, U, RuntimeException> get() {
                return CollectorState.reducing(seed, function);
            }
        });
    }

    public static <T, R, E extends Exception> Collector<T, R, E> reducing(final BinaryOperatorDefinition<T> operator, final OptionalVisitor<? super T, R, E> visitor) {
        return new Collector<T, R, E>(new Supplier<Collecting<T, R, E>>() {
            @Override
            public Collecting<T, R, E> get() {
                return CollectorState.reducing(operator, visitor);
            }
        });
    }

    public static <R, E extends Exception> Collector<String, R, E> joiningStrings(final String separator, final OptionalVisitor<String, R, E> visitor) {
        return new Collector<String, R, E>(new Supplier<Collecting<String, R, E>>() {
            @Override
            public Collecting<String, R, E> get() {
                return CollectorState.joiningStrings(separator, visitor);
            }
        });
    }

    public static Collector<String, String, RuntimeException> joiningStrings() {
        return new Collector<String, String, RuntimeException>(new Supplier<Collecting<String, String, RuntimeException>>() {
            @Override
            public Collecting<String, String, RuntimeException> get() {
                return CollectorState.joiningStrings();
            }
        });
    }

    public static <T> Collector<T, List<T>, RuntimeException> toList() {
        return new Collector<T, List<T>, RuntimeException>(new Supplier<Collecting<T, List<T>, RuntimeException>>() {
            @Override
            public Collecting<T, List<T>, RuntimeException> get() {
                return CollectorState.toList();
            }
        });
    }

    public static <T extends Comparable<? super T>> Collector<T, List<T>, RuntimeException> toSortedList() {
        return new Collector<T, List<T>, RuntimeException>(new Supplier<Collecting<T, List<T>, RuntimeException>>() {
            @Override
            public Collecting<T, List<T>, RuntimeException> get() {
                return CollectorState.<T>toSortedList();
            }
        });
    }

    /**
     * @param limit limit on the size of resulting list.
     *        If stream contains more than limit elements only first limit elements are returned
     *
     * @return the list of several first minimal elements
     */
    public static <T extends Comparable<? super T>> Collector<T, List<T>, RuntimeException> toSortedList(final int limit) {
        return new Collector<T, List<T>, RuntimeException>(new Supplier<Collecting<T, List<T>, RuntimeException>>() {
            @Override
            public Collecting<T, List<T>, RuntimeException> get() {
                return CollectorState.<T>toSortedList(limit);
            }
        });
    }

    public static <T> Collector<T, HashSet<T>, RuntimeException> toHashSet() {
        return new Collector<T, HashSet<T>, RuntimeException>(new Supplier<Collecting<T, HashSet<T>, RuntimeException>>() {
            @Override
            public Collecting<T, HashSet<T>, RuntimeException> get() {
                return CollectorState.toHashSet();
            }
        });
    }

    public static <T> Collector<T, TreeSet<T>, RuntimeException> toTreeSet() {
        return new Collector<T, TreeSet<T>, RuntimeException>(new Supplier<Collecting<T, TreeSet<T>, RuntimeException>>() {
            @Override
            public Collecting<T, TreeSet<T>, RuntimeException> get() {
                return CollectorState.toTreeSet();
            }
        });
    }

    public static <T, R, E extends Exception> Collector<T, R, E> findFirst(final OptionalVisitor<? super T, R, E> visitor) {
        return new Collector<T, R, E>(new Supplier<Collecting<T, R, E>>() {
            @Override
            public Collecting<T, R, E> get() {
                return CollectorState.findFirst(visitor);
            }
        });
    }

    public static <T, R extends Collection<T>> Collector<T, R, RuntimeException> toCollection(final Supplier<R> factory) {
        return new Collector<T, R, RuntimeException>(new Supplier<Collecting<T, R, RuntimeException>>() {
            @Override
            public Collecting<T, R, RuntimeException> get() {
                return CollectorState.toCollection(factory.get());
            }
        });
    }

    public static <I, K, V, R extends Map<K, V>> Collector<I, R, RuntimeException> toMap(final Applicable<? super I, ? extends K> function, final Supplier<? extends Collecting<I, V, ? extends RuntimeException>> collector, final Supplier<R> factory) {
        return new Collector<I, R, RuntimeException>(new Supplier<Collecting<I, R, RuntimeException>>() {
            @Override
            public Collecting<I, R, RuntimeException> get() {
                return CollectorState.toMap(function, collector, factory);
            }
        });
    }

    public static <I, K, V> Collector<I, HashMap<K, V>, RuntimeException> toHashMap(final Applicable<? super I, ? extends K> function, final Supplier<? extends Collecting<I, V, ? extends RuntimeException>> collector) {
        return new Collector<I, HashMap<K, V>, RuntimeException>(new Supplier<Collecting<I, HashMap<K, V>, RuntimeException>>() {
            @Override
            public Collecting<I, HashMap<K, V>, RuntimeException> get() {
                return CollectorState.toHashMap(function, collector);
            }
        });
    }

    public static <I, K, V> Collector<I, TreeMap<K, V>, RuntimeException> toTreeMap(final Applicable<? super I, ? extends K> function, final Supplier<? extends Collecting<I, V, ? extends RuntimeException>> collector) {
        return new Collector<I, TreeMap<K, V>, RuntimeException>(new Supplier<Collecting<I, TreeMap<K, V>, RuntimeException>>() {
            @Override
            public Collecting<I, TreeMap<K, V>, RuntimeException> get() {
                return CollectorState.toTreeMap(function, collector);
            }
        });
    }

    public static <T> Collector<T, Integer, RuntimeException> counting() {
        return new Collector<T, Integer, RuntimeException>(new Supplier<Collecting<T, Integer, RuntimeException>>() {
            @Override
            public Collecting<T, Integer, RuntimeException> get() {
                return CollectorState.counting();
            }
        });
    }

    public static <T extends Comparable<? super T>, R, E extends Exception> Collector<T, R, E> maximum(final OptionalVisitor<T, R, E> optionalVisitor) {
        return new Collector<T, R, E>(new Supplier<Collecting<T, R, E>>() {
            @Override
            public Collecting<T, R, E> get() {
                return CollectorState.<T, R, E>maximum(optionalVisitor);
            }
        });
    }

    public static <T extends Comparable<? super T>, R, E extends Exception> Collector<T, R, E> minimum(final OptionalVisitor<T, R, E> optionalVisitor) {
        return new Collector<T, R, E>(new Supplier<Collecting<T, R, E>>() {
            @Override
            public Collecting<T, R, E> get() {
                return CollectorState.<T, R, E>minimum(optionalVisitor);
            }
        });
    }

    public static <T, R, E extends Exception> Collector<T, R, E> maximum(final Comparator<? super T> comparator, final OptionalVisitor<T, R, E> optionalVisitor) {
        return new Collector<T, R, E>(new Supplier<Collecting<T, R, E>>() {
            @Override
            public Collecting<T, R, E> get() {
                return CollectorState.maximum(comparator, optionalVisitor);
            }
        });
    }

    public static <T, R, E extends Exception> Collector<T, R, E> minimum(final Comparator<? super T> comparator, final OptionalVisitor<T, R, E> optionalVisitor) {
        return new Collector<T, R, E>(new Supplier<Collecting<T, R, E>>() {
            @Override
            public Collecting<T, R, E> get() {
                return CollectorState.minimum(comparator, optionalVisitor);
            }
        });
    }

    public static Collector<Integer, Integer, RuntimeException> summingInt() {
        return new Collector<Integer, Integer, RuntimeException>(new Supplier<Collecting<Integer, Integer, RuntimeException>>() {
            @Override
            public Collecting<Integer, Integer, RuntimeException> get() {
                return CollectorState.summingInt();
            }
        });
    }

    public static Collector<Long, Long, RuntimeException> summingLong() {
        return new Collector<Long, Long, RuntimeException>(new Supplier<Collecting<Long, Long, RuntimeException>>() {
            @Override
            public Collecting<Long, Long, RuntimeException> get() {
                return CollectorState.summingLong();
            }
        });
    }

    public static Collector<Double, Double, RuntimeException> summingDouble() {
        return new Collector<Double, Double, RuntimeException>(new Supplier<Collecting<Double, Double, RuntimeException>>() {
            @Override
            public Collecting<Double, Double, RuntimeException> get() {
                return CollectorState.summingDouble();
            }
        });
    }

    public static Collector<Integer, Integer, RuntimeException> productingInt() {
        return new Collector<Integer, Integer, RuntimeException>(new Supplier<Collecting<Integer, Integer, RuntimeException>>() {
            @Override
            public Collecting<Integer, Integer, RuntimeException> get() {
                return CollectorState.productingInt();
            }
        });
    }

    public static Collector<Long, Long, RuntimeException> productingLong() {
        return new Collector<Long, Long, RuntimeException>(new Supplier<Collecting<Long, Long, RuntimeException>>() {
            @Override
            public Collecting<Long, Long, RuntimeException> get() {
                return CollectorState.productingLong();
            }
        });
    }

    public static Collector<Double, Double, RuntimeException> productingDouble() {
        return new Collector<Double, Double, RuntimeException>(new Supplier<Collecting<Double, Double, RuntimeException>>() {
            @Override
            public Collecting<Double, Double, RuntimeException> get() {
                return CollectorState.productingDouble();
            }
        });
    }

    private final Supplier<? extends Collecting<T, R, E>> collector;

    public Collector(Supplier<? extends Collecting<T, R, E>> collector) {
        this.collector = collector;
    }

    @Override
    public Collecting<T, R, E> get() {
        return collector.get();
    }

    public Collector<T, R, E> limiting(final int limit) {
        return new Collector<T, R, E>(new Supplier<Collecting<T, R, E>>() {
            @Override
            public Collecting<T, R, E> get() {
                return CollectorState.of(collector.get()).limiting(limit);
            }
        });
    }

    public Collector<T, R, E> skipping(final int offset) {
        return new Collector<T, R, E>(new Supplier<Collecting<T, R, E>>() {
            @Override
            public Collecting<T, R, E> get() {
                return CollectorState.of(collector.get()).skipping(offset);
            }
        });
    }

    public Collector<T, R, E> filtering(final Evaluatable<? super T> predicate) {
        return new Collector<T, R, E>(new Supplier<Collecting<T, R, E>>() {
            @Override
            public Collecting<T, R, E> get() {
                return CollectorState.of(collector.get()).filtering(predicate);
            }
        });
    }

    public <U> Collector<U, R, E> mapping(final Applicable<U, ? extends T> function) {
        return new Collector<U, R, E>(new Supplier<Collecting<U, R, E>>() {
            @Override
            public Collecting<U, R, E> get() {
                return CollectorState.of(collector.get()).mapping(function);
            }
        });
    }

    public <U> Collector<T, U, E> finallyTransforming(final Applicable<? super R, U> function) {
        return new Collector<T, U, E>(new Supplier<Collecting<T, U, E>>() {
            @Override
            public Collecting<T, U, E> get() {
                return CollectorState.of(collector.get()).finallyTransforming(function);
            }
        });
    }
}
