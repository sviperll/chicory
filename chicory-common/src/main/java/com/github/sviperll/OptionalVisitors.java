/*
 * Copyright (C) 2013 Victor Nazarov <asviraspossible@gmail.com>
 */

package com.github.sviperll;

import java.util.NoSuchElementException;

public class OptionalVisitors {
    public static <T> OptionalVisitor<T, T, NoSuchElementException> throwNoSuchElementException() {
        return new OptionalVisitor<T, T, NoSuchElementException>() {
            @Override
            public T present(T value) throws NoSuchElementException {
                return value;
            }

            @Override
            public T missing() throws NoSuchElementException {
                throw new NoSuchElementException();
            }
        };
    }

    public static <T> OptionalVisitor<T, T, RuntimeException> returnNull() {
        return new OptionalVisitor<T, T, RuntimeException>() {
            @Override
            public T present(T value) throws RuntimeException {
                return value;
            }

            @Override
            public T missing() throws RuntimeException {
                return null;
            }
        };
    }

    public static <T> OptionalVisitor<T, T, RuntimeException> returnDefault(final T defaultValue) {
        return new OptionalVisitor<T, T, RuntimeException>() {
            @Override
            public T present(T value) throws RuntimeException {
                return value;
            }

            @Override
            public T missing() throws RuntimeException {
                return defaultValue;
            }
        };
    }

    private OptionalVisitors() {
    }
}
