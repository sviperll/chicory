package com.github.sviperll;

public interface TypeStructure2<T, U, V> {
    U getField1(T instance);

    V getField2(T instance);

    T createValue(U field1, V field2);
}
