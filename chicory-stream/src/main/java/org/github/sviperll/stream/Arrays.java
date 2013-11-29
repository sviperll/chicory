/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class Arrays {
    public static <T> Stream<T> asStream(final T[] array) {
        return Stream.valueOf(new Streamable<T>() {

            @Override
            public void forEach(SaturableConsuming<? super T> consumer) {
                for (T value: array)
                    consumer.accept(value);
            }
        });
    }

    private Arrays() {
    }
}
