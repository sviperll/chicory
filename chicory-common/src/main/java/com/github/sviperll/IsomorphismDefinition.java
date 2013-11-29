package com.github.sviperll;

public interface IsomorphismDefinition<T, U> {
    U forward(T object);

    T backward(U object);
}
