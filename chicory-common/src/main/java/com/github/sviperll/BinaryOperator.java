/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class BinaryOperator<T> implements BinaryOperatorDefinition<T> {
    private final BinaryOperatorDefinition<T> operator;
    public BinaryOperator(BinaryOperatorDefinition<T> operator) {
        this.operator = operator;
    }

    @Override
    public T apply(T argument1, T argument2) {
        return operator.apply(argument1, argument2);
    }
}
