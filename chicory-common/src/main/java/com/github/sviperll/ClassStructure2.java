package com.github.sviperll;

public interface ClassStructure2<T, U, V> {
    U getField1(T instance);

    V getField2(T instance);

    T createInstance(U field1, V field2);
}
