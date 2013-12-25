/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

import com.github.sviperll.Applicable;
import com.github.sviperll.Evaluatable;
import com.github.sviperll.Function;
import com.github.sviperll.Predicate;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class SaturableConsumers {
    public static <T> SaturableConsumer<T> valueOf(final SaturableConsuming<T> consuming) {
        if (consuming instanceof SaturableConsumer)
            return (SaturableConsumer<T>)consuming;
        else
            return new FusingSaturableConsumer<>(consuming);
    }

    private SaturableConsumers() {
    }

    private static class FusingSaturableConsumer<T> implements SaturableConsumer<T> {
        private final SaturableConsuming<T> consuming;
        public FusingSaturableConsumer(SaturableConsuming<T> consuming) {
            this.consuming = consuming;
        }

        @Override
        public SaturableConsumer<T> filtering(final Evaluatable<? super T> predicate) {
            return new SaturableConsumer<T>() {
                @Override
                public SaturableConsumer<T> filtering(Evaluatable<? super T> predicate2) {
                    return new FusingSaturableConsumer<>(consuming).filtering(Predicate.and(predicate2, predicate));
                }

                @Override
                public SaturableConsumer<T> limiting(int limit) {
                    return new FusingSaturableConsumer<>(this).limiting(limit);
                }

                @Override
                public <U> SaturableConsumer<U> mapping(Applicable<U, ? extends T> function) {
                    return new FusingSaturableConsumer<>(this).mapping(function);
                }

                @Override
                public SaturableConsumer<T> skipping(int offset) {
                    return new FusingSaturableConsumer<>(this).skipping(offset);
                }

                @Override
                public void accept(T value) {
                    if (predicate.evaluate(value)) {
                        consuming.accept(value);
                    }
                }

                @Override
                public boolean needsMore() {
                    return consuming.needsMore();
                }
            };
        }

        @Override
        public SaturableConsumer<T> limiting(final int limit) {
            return new SaturableConsumer<T>() {
                int count = 0;

                @Override
                public void accept(T value) {
                    if (count < limit) {
                        consuming.accept(value);
                    }
                    count++;
                }

                @Override
                public boolean needsMore() {
                    return count < limit && consuming.needsMore();
                }

                @Override
                public SaturableConsumer<T> filtering(Evaluatable<? super T> predicate) {
                    return new FusingSaturableConsumer<>(this).filtering(predicate);
                }

                @Override
                public SaturableConsumer<T> limiting(int limit2) {
                    return new FusingSaturableConsumer<>(consuming).limiting(Math.min(limit, limit2));
                }

                @Override
                public <U> SaturableConsumer<U> mapping(Applicable<U, ? extends T> function) {
                    return new FusingSaturableConsumer<>(this).mapping(function);
                }

                @Override
                public SaturableConsumer<T> skipping(int offset) {
                    return new FusingSaturableConsumer<>(this).skipping(offset);
                }

            };
        }

        @Override
        public <U> SaturableConsumer<U> mapping(final Applicable<U, ? extends T> function) {
            return new SaturableConsumer<U>() {
                @Override
                public void accept(U value) {
                    consuming.accept(function.apply(value));
                }

                @Override
                public boolean needsMore() {
                    return consuming.needsMore();
                }

                @Override
                public SaturableConsumer<U> filtering(final Evaluatable<? super U> predicate) {
                    return new FusingSaturableConsumer<>(this).filtering(predicate);
                }

                @Override
                public SaturableConsumer<U> limiting(int limit) {
                    return new FusingSaturableConsumer<>(this).limiting(limit);
                }

                @Override
                public <V> SaturableConsumer<V> mapping(Applicable<V, ? extends U> function2) {
                    return new FusingSaturableConsumer<>(consuming).mapping(Function.valueOf(function).composeWith(function2));
                }

                @Override
                public SaturableConsumer<U> skipping(int offset) {
                    return new FusingSaturableConsumer<>(this).skipping(offset);
                }
            };
        }

        @Override
        public SaturableConsumer<T> skipping(final int offset) {
            return new SaturableConsumer<T>() {
                int count = 0;

                @Override
                public void accept(T value) {
                    if (count >= offset) {
                        consuming.accept(value);
                    }
                    count++;
                }

                @Override
                public boolean needsMore() {
                    return count < offset || consuming.needsMore();
                }

                @Override
                public SaturableConsumer<T> filtering(Evaluatable<? super T> predicate) {
                    return new FusingSaturableConsumer<>(this).filtering(predicate);
                }

                @Override
                public SaturableConsumer<T> limiting(int limit) {
                    return new FusingSaturableConsumer<>(this).limiting(limit);
                }

                @Override
                public <U> SaturableConsumer<U> mapping(Applicable<U, ? extends T> function) {
                    return new FusingSaturableConsumer<>(this).mapping(function);
                }

                @Override
                public SaturableConsumer<T> skipping(int offset2) {
                    return new FusingSaturableConsumer<>(consuming).skipping(offset + offset2);
                }
            };
        }

        @Override
        public void accept(T value) {
            consuming.accept(value);
        }

        @Override
        public boolean needsMore() {
            return consuming.needsMore();
        }
    }
}
