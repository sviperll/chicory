package com.github.sviperll.repository;

/**
 * @param <T> tuple type
 * @param <U> column value data-type
 */
public interface AtomicStorableClassComponent<T, U> {
    TableColumn<U> getColumn();

    U getComponent(T compound);
}
