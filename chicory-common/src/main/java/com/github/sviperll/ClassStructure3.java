package com.github.sviperll;

public interface ClassStructure3<T, U, V, W> {
    U getField1(T instance);

    V getField2(T instance);

    W getField3(T instance);

    T createInstance(U field1, V field2, W field3);
}
