/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

import com.github.sviperll.Applicable;
import com.github.sviperll.Evaluatable;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class SaturableConsumer<T> implements SaturableConsuming<T> {
    public static <T> SaturableConsumer<T> valueOf(SaturableConsuming<T> consuming) {
        if (consuming instanceof SaturableConsumer)
            return (SaturableConsumer<T>)consuming;
        else
            return new SaturableConsumer<>(consuming);
    }

    private final SaturableConsuming<T> consuming;
    private SaturableConsumer(SaturableConsuming<T> consuming) {
        this.consuming = consuming;
    }

    @Override
    public void accept(T value) {
        consuming.accept(value);
    }

    @Override
    public boolean needsMore() {
        return consuming.needsMore();
    }

    public SaturableConsumer<T> limiting(final int limit) {
        return new SaturableConsumer<>(new SaturableConsuming<T>() {
            int count = 0;

            @Override
            public void accept(T value) {
                if (count < limit)
                    consuming.accept(value);
                count++;
            }

            @Override
            public boolean needsMore() {
                return count < limit && consuming.needsMore();
            }
        });
    }

    public SaturableConsumer<T> skipping(final int offset) {
        return new SaturableConsumer<>(new SaturableConsuming<T>() {
            int count = 0;

            @Override
            public void accept(T value) {
                if (count >= offset)
                    consuming.accept(value);
                count++;
            }

            @Override
            public boolean needsMore() {
                return count >= offset && consuming.needsMore();
            }
        });
    }

    public SaturableConsumer<T> filtering(final Evaluatable<? super T> predicate) {
        return new SaturableConsumer<>(new SaturableConsuming<T>() {
            @Override
            public void accept(T value) {
                if (predicate.evaluate(value))
                    consuming.accept(value);
            }

            @Override
            public boolean needsMore() {
                return consuming.needsMore();
            }
        });
    }

    public <U> SaturableConsumer<U> mapping(final Applicable<U, ? extends T> function) {
        return new SaturableConsumer<>(new SaturableConsuming<U>() {
            @Override
            public void accept(U value) {
                consuming.accept(function.apply(value));
            }

            @Override
            public boolean needsMore() {
                return consuming.needsMore();
            }
        });
    }
}
