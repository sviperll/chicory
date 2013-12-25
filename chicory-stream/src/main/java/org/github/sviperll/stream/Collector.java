/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

import com.github.sviperll.Applicable;
import com.github.sviperll.BiApplicable;
import com.github.sviperll.BinaryOperatorDefinition;
import com.github.sviperll.Evaluatable;
import com.github.sviperll.OptionalVisitor;
import com.github.sviperll.Supplier;
import java.util.Collection;
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
        return new Collector<>(new Supplier<Collecting<T, R, E>>() {
            @Override
            public Collecting<T, R, E> get() {
                return CollectorState.optional(notOptionalCollector, visitor);
            }
        });
    }

    public static <T, U> Collector<T, U, RuntimeException> reducing(final U seed, final BiApplicable<U, ? super T, U> function) {
        return new Collector<>(new Supplier<Collecting<T, U, RuntimeException>>() {
            @Override
            public Collecting<T, U, RuntimeException> get() {
                return CollectorState.reducing(seed, function);
            }
        });
    }

    public static <T, R, E extends Exception> Collector<T, R, E> reducing(final BinaryOperatorDefinition<T> operator, final OptionalVisitor<? super T, R, E> visitor) {
        return new Collector<>(new Supplier<Collecting<T, R, E>>() {
            @Override
            public Collecting<T, R, E> get() {
                return CollectorState.reducing(operator, visitor);
            }
        });
    }

    public static <T> Collector<T, List<T>, RuntimeException> toList() {
        return new Collector<>(new Supplier<Collecting<T, List<T>, RuntimeException>>() {
            @Override
            public Collecting<T, List<T>, RuntimeException> get() {
                return CollectorState.toList();
            }
        });
    }

    public static <T extends Comparable<? super T>> Collector<T, List<T>, RuntimeException> toSortedList() {
        return new Collector<>(new Supplier<Collecting<T, List<T>, RuntimeException>>() {
            @Override
            public Collecting<T, List<T>, RuntimeException> get() {
                return CollectorState.toSortedList();
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
        return new Collector<>(new Supplier<Collecting<T, List<T>, RuntimeException>>() {
            @Override
            public Collecting<T, List<T>, RuntimeException> get() {
                return CollectorState.toSortedList(limit);
            }
        });
    }

    public static <T> Collector<T, HashSet<T>, RuntimeException> toHashSet() {
        return new Collector<>(new Supplier<Collecting<T, HashSet<T>, RuntimeException>>() {
            @Override
            public Collecting<T, HashSet<T>, RuntimeException> get() {
                return CollectorState.toHashSet();
            }
        });
    }

    public static <T> Collector<T, TreeSet<T>, RuntimeException> toTreeSet() {
        return new Collector<>(new Supplier<Collecting<T, TreeSet<T>, RuntimeException>>() {
            @Override
            public Collecting<T, TreeSet<T>, RuntimeException> get() {
                return CollectorState.toTreeSet();
            }
        });
    }

    static <T, R, E extends Exception> Collector<T, R, E> findFirst(final OptionalVisitor<? super T, R, E> visitor) {
        return new Collector<>(new Supplier<Collecting<T, R, E>>() {
            @Override
            public Collecting<T, R, E> get() {
                return CollectorState.findFirst(visitor);
            }
        });
    }

    public static <T, R extends Collection<T>> Collector<T, R, RuntimeException> toCollection(final Supplier<R> factory) {
        return new Collector<>(new Supplier<Collecting<T, R, RuntimeException>>() {
            @Override
            public Collecting<T, R, RuntimeException> get() {
                return CollectorState.toCollection(factory.get());
            }
        });
    }

    public static <I, K, V, R extends Map<K, V>> Collector<I, R, RuntimeException> toMap(final Applicable<? super I, ? extends K> function, final Supplier<? extends Collecting<I, V, ? extends RuntimeException>> collector, final Supplier<R> factory) {
        return new Collector<>(new Supplier<Collecting<I, R, RuntimeException>>() {
            @Override
            public Collecting<I, R, RuntimeException> get() {
                return CollectorState.toMap(function, collector, factory);
            }
        });
    }

    public static <I, K, V> Collector<I, HashMap<K, V>, RuntimeException> toHashMap(final Applicable<? super I, ? extends K> function, final Supplier<? extends Collecting<I, V, ? extends RuntimeException>> collector) {
        return new Collector<>(new Supplier<Collecting<I, HashMap<K, V>, RuntimeException>>() {
            @Override
            public Collecting<I, HashMap<K, V>, RuntimeException> get() {
                return CollectorState.toHashMap(function, collector);
            }
        });
    }

    public static <I, K, V> Collector<I, TreeMap<K, V>, RuntimeException> toTreeMap(final Applicable<? super I, ? extends K> function, final Supplier<? extends Collecting<I, V, ? extends RuntimeException>> collector) {
        return new Collector<>(new Supplier<Collecting<I, TreeMap<K, V>, RuntimeException>>() {
            @Override
            public Collecting<I, TreeMap<K, V>, RuntimeException> get() {
                return CollectorState.toTreeMap(function, collector);
            }
        });
    }

    public static <T> Collector<T, Integer, RuntimeException> counting() {
        return new Collector<>(new Supplier<Collecting<T, Integer, RuntimeException>>() {
            @Override
            public Collecting<T, Integer, RuntimeException> get() {
                return CollectorState.counting();
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
        return new Collector<>(new Supplier<Collecting<T, R, E>>() {
            @Override
            public Collecting<T, R, E> get() {
                return CollectorState.valueOf(collector.get()).limiting(limit);
            }
        });
    }

    public Collector<T, R, E> skipping(final int offset) {
        return new Collector<>(new Supplier<Collecting<T, R, E>>() {
            @Override
            public Collecting<T, R, E> get() {
                return CollectorState.valueOf(collector.get()).skipping(offset);
            }
        });
    }

    public Collector<T, R, E> filtering(final Evaluatable<? super T> predicate) {
        return new Collector<>(new Supplier<Collecting<T, R, E>>() {
            @Override
            public Collecting<T, R, E> get() {
                return CollectorState.valueOf(collector.get()).filtering(predicate);
            }
        });
    }

    public <U> Collector<U, R, E> mapping(final Applicable<U, ? extends T> function) {
        return new Collector<>(new Supplier<Collecting<U, R, E>>() {
            @Override
            public Collecting<U, R, E> get() {
                return CollectorState.valueOf(collector.get()).mapping(function);
            }
        });
    }

    public <U> Collector<T, U, E> finallyTransforming(final Applicable<? super R, U> function) {
        return new Collector<>(new Supplier<Collecting<T, U, E>>() {
            @Override
            public Collecting<T, U, E> get() {
                return CollectorState.valueOf(collector.get()).finallyTransforming(function);
            }
        });
    }
}
