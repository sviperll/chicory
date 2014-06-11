package com.github.sviperll.repository;

/**
 * @param <T> tuple type
 * @param <U> column value data-type
 */
public interface AtomicStorableValueComponent<T, U> {
    TableColumnDefinition<U> getColumn();

    U getComponent(T compound);
}
