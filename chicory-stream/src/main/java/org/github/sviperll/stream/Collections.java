/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class Collections {
    public static <T> Stream<T> asStream(final Iterable<T> collection) {
        return Stream.valueOf(new Streamable<T>() {
            @Override
            public void forEach(SaturableConsuming<? super T> consumer) {
                for (T value: collection) {
                    consumer.accept(value);
                }
            }
        });
    }

    private Collections() {
    }
}
