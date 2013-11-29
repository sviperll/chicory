/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class BiFunction<T, U, R> implements BiApplicable<T, U, R> {
    private final BiApplicable<T, U, R> function;
    public BiFunction(BiApplicable<T, U, R> function) {
        this.function = function;
    }

    @Override
    public R apply(T argument1, U argument2) {
        return function.apply(argument1, argument2);
    }
}
