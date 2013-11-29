package com.github.sviperll;

public interface ClassStructure4<T, U, V, W, X> {
    U getField1(T instance);

    V getField2(T instance);

    W getField3(T instance);

    X getField4(T instance);

    T createInstance(U field1, V field2, W field3, X field4);
}
