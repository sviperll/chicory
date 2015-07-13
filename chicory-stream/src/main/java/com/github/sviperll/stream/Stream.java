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
import com.github.sviperll.Consumer;
import com.github.sviperll.Evaluatable;
import com.github.sviperll.Function;
import com.github.sviperll.OptionalVisitor;
import com.github.sviperll.Supplier;
import com.github.sviperll.UnaryOperatorDefinition;

/**
 * Objects that provides stream processing.
 *
 * @see Streamable
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class Stream<T> implements Streamable<T> {
    public static <T> Stream<T> of(final Streamable<T> streamable) {
        if (streamable instanceof Stream)
            return (Stream<T>)streamable;
        else
            return new Stream<T>(streamable);
    }

    public static <T> Stream<T> generate(final Supplier<T> supplier) {
        return new Stream<T>(new Streamable<T>() {
            @Override
            public void forEach(SaturableConsuming<? super T> consumer) {
                consumer.accept(supplier.get());
            }
        });
    }

    public static <T> Stream<T> iterate(final T seed, final UnaryOperatorDefinition<T> function) {
        return new Stream<T>(new Streamable<T>() {
            private T value = seed;
            @Override
            public void forEach(SaturableConsuming<? super T> consumer) {
                consumer.accept(value);
                value = function.apply(seed);
            }
        });
    }

    public static <T> Stream<T> flatten(final Streamable<? extends Streamable<T>> stream) {
        return typedFlatten(stream);
    }

    private static <T, S extends Streamable<T>> Stream<T> typedFlatten(final Streamable<S> stream) {
        return new Stream<T>(new Streamable<T>() {
            @Override
            public void forEach(final SaturableConsuming<? super T> consumer) {
                stream.forEach(new SaturableConsuming<S>() {
                    @Override
                    public void accept(S substream) {
                        substream.forEach(consumer);
                    }

                    @Override
                    public boolean needsMore() {
                        return consumer.needsMore();
                    }
                });
            }
        });
    }

    public static <T> Stream<T> empty() {
        return Stream.of(new Streamable<T>() {
            @Override
            public void forEach(SaturableConsuming<? super T> consumer) {
            }
        });
    }

    public static <T> Stream<T> ofElements(final T element1) {
        return Stream.of(new Streamable<T>() {
            @Override
            public void forEach(SaturableConsuming<? super T> consumer) {
                consumer.accept(element1);
            }
        });
    }

    public static <T> Stream<T> ofElements(final T element1, final T element2) {
        return Stream.of(new Streamable<T>() {
            @Override
            public void forEach(SaturableConsuming<? super T> consumer) {
                consumer.accept(element1);
                consumer.accept(element2);
            }
        });
    }

    public static <T> Stream<T> ofElements(final T element1, final T element2, final T element3) {
        return Stream.of(new Streamable<T>() {
            @Override
            public void forEach(SaturableConsuming<? super T> consumer) {
                consumer.accept(element1);
                consumer.accept(element2);
                consumer.accept(element3);
            }
        });
    }

    public static <T> Stream<T> ofElements(final T element1, final T element2, final T element3, final T element4) {
        return Stream.of(new Streamable<T>() {
            @Override
            public void forEach(SaturableConsuming<? super T> consumer) {
                consumer.accept(element1);
                consumer.accept(element2);
                consumer.accept(element3);
                consumer.accept(element4);
            }
        });
    }

    public static <T> Stream<T> ofElements(final T element1, final T element2, final T element3, final T element4, final T element5) {
        return Stream.of(new Streamable<T>() {
            @Override
            public void forEach(SaturableConsuming<? super T> consumer) {
                consumer.accept(element1);
                consumer.accept(element2);
                consumer.accept(element3);
                consumer.accept(element4);
                consumer.accept(element5);
            }
        });
    }

    private final Streamable<T> streamable;
    private Stream(Streamable<T> streamable) {
        this.streamable = streamable;
    }

    public CloseableIterator<T> openIterator() {
        return StreamIterator.createInstance(streamable);
    }

    @Override
    public void forEach(SaturableConsuming<? super T> consumer) {
        streamable.forEach(consumer);
    }

    public void forEach(final Consumer<? super T> consumer) {
        streamable.forEach(new SaturableConsuming<T>() {
            @Override
            public void accept(T value) {
                consumer.accept(value);
            }

            @Override
            public boolean needsMore() {
                return true;
            }
        });
    }

    public <U> Stream<U> map(final Applicable<? super T, U> function) {
        return new Stream<U>(new Streamable<U>() {
            @Override
            public void forEach(SaturableConsuming<? super U> consumer) {
                streamable.forEach(SaturableConsumer.of(consumer).mapping(function));
            }
        });
    }

    public Stream<T> filter(final Evaluatable<? super T> predicate) {
        return new Stream<T>(new Streamable<T>() {
            @Override
            public void forEach(SaturableConsuming<? super T> consumer) {
                streamable.forEach(SaturableConsumer.of(consumer).mapping(Function.<T>identity()).filtering(predicate));
            }

        });
    }

    public <U> U reduce(final U seed, final BiApplicable<U, ? super T, U> function) {
        return collect(Collector.reducing(seed, function));
    }

    public <R, E extends Exception> R reduce(final BinaryOperatorDefinition<T> operator, OptionalVisitor<? super T, R, E> visitor) throws E {
        return collect(Collector.reducing(operator, visitor));
    }

    public <R> Stream<R> flatMap(Applicable<? super T, ? extends Streamable<R>> function) {
        return flatten(map(function));
    }

    public Stream<T> skip(final int offset) {
        return new Stream<T>(new Streamable<T>() {
            @Override
            public void forEach(SaturableConsuming<? super T> consumer) {
                streamable.forEach(SaturableConsumer.of(consumer).skipping(offset));
            }
        });
    }

    public Stream<T> limit(final int size) {
        return new Stream<T>(new Streamable<T>() {
            @Override
            public void forEach(SaturableConsuming<? super T> consumer) {
                streamable.forEach(SaturableConsumer.of(consumer).limiting(size));
            }
        });
    }

    public <R, E extends Exception> R collect(Supplier<? extends Collecting<? super T, R, E>> collector) throws E {
        Collecting<? super T, R, E> state = collector.get();
        streamable.forEach(state);
        return state.get();
    }

    public <R, E extends Exception> R findFirst(OptionalVisitor<? super T, R, E> visitor) throws E {
        return collect(Collector.findFirst(visitor));
    }

    public int count() {
        return collect(Collector.counting());
    }
}
