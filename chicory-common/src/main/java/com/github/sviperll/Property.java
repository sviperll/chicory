/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class Property<T> {
    private T value;
    public Property(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public T set(T newValue) {
        T oldValue = value;
        this.value = newValue;
        return oldValue;
    }
}
