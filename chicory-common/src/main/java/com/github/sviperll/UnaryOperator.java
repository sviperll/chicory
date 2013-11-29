/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class UnaryOperator<T> implements UnaryOperatorDefinition<T> {
    private final UnaryOperatorDefinition<T> operator;
    public UnaryOperator(UnaryOperatorDefinition<T> operator) {
        this.operator = operator;
    }

    @Override
    public T apply(T t) {
        return operator.apply(t);
    }

    public Function<T, T> asFunction() {
        return new Function<>(operator);
    }
}
