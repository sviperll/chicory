package com.github.sviperll;

public interface TypeStructure3<T, U, V, W> {
    U getField1(T instance);

    V getField2(T instance);

    W getField3(T instance);

    T createValue(U field1, V field2, W field3);
}
