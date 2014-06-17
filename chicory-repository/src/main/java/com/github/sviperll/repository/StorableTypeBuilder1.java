/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.repository;

import com.github.sviperll.IsomorphismDefinition;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class StorableTypeBuilder1<T, U> {

    private static <T, U, V> AtomicStorableComponent<T, V> convertElement(final AtomicStorableComponent<U, V> element,
                                                                               final IsomorphismDefinition<T, U> isomorphism) {
        return new AtomicStorableComponent<T, V>() {
            @Override
            public TableColumnDefinition<V> getColumn() {
                return element.getColumn();
            }

            @Override
            public V getComponent(T tuple) {
                return element.getComponent(isomorphism.forward(tuple));
            }
        };
    }
    private final IsomorphismDefinition<T, U> isomorphism;

    public StorableTypeBuilder1(IsomorphismDefinition<T, U> isomorphism) {
        this.isomorphism = isomorphism;
    }

    public StorableType<T> build(final StorableTypeDefinition<U> base) {
        final List<AtomicStorableComponent<T, ?>> elements = new ArrayList<>();
        for (AtomicStorableComponent<U, ?> element : base.getAtomicComponents()) {
            elements.add(convertElement(element, isomorphism));
        }
        return StorableType.of(new StorableTypeDefinition<T>() {
            @Override
            public List<? extends AtomicStorableComponent<T, ?>> getAtomicComponents() {
                return elements;
            }

            @Override
            public T createInstance(ResultSet resultSet) throws SQLException {
                return isomorphism.backward(base.createInstance(resultSet));
            }
        });
    }

}
