/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.repository;

import com.github.sviperll.TypeStructure3;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class StorableTypeBuilder3<T, U, V, W> {
    private static <T, U, V> AtomicStorableComponent<T, V> convertElement1(final AtomicStorableComponent<U, V> element, final TypeStructure3<T, U, ?, ?> structure) {
        return new AtomicStorableComponent<T, V>() {

            @Override
            public TableColumnDefinition<V> getColumn() {
                return element.getColumn();
            }

            @Override
            public V getComponent(T tuple) {
                return element.getComponent(structure.getField1(tuple));
            }
        };
    }

    private static <T, U, V> AtomicStorableComponent<T, V> convertElement2(final AtomicStorableComponent<U, V> element, final TypeStructure3<T, ?, U, ?> structure) {
        return new AtomicStorableComponent<T, V>() {

            @Override
            public TableColumnDefinition<V> getColumn() {
                return element.getColumn();
            }

            @Override
            public V getComponent(T tuple) {
                return element.getComponent(structure.getField2(tuple));
            }
        };
    }

    private static <T, U, V> AtomicStorableComponent<T, V> convertElement3(final AtomicStorableComponent<U, V> element, final TypeStructure3<T, ?, ?, U> structure) {
        return new AtomicStorableComponent<T, V>() {

            @Override
            public TableColumnDefinition<V> getColumn() {
                return element.getColumn();
            }

            @Override
            public V getComponent(T tuple) {
                return element.getComponent(structure.getField3(tuple));
            }
        };
    }

    private final TypeStructure3<T, U, V, W> structure;

    public StorableTypeBuilder3(TypeStructure3<T, U, V, W> structure) {
        this.structure = structure;
    }

    public StorableType<T> build(final StorableTypeDefinition<U> field1, final StorableTypeDefinition<V> field2, final StorableTypeDefinition<W> field3) {
        final List<AtomicStorableComponent<T, ?>> elements = new ArrayList<>();
        for (AtomicStorableComponent<U, ?> element: field1.getAtomicComponents()) {
            elements.add(convertElement1(element, structure));
        }
        for (AtomicStorableComponent<V, ?> element: field2.getAtomicComponents()) {
            elements.add(convertElement2(element, structure));
        }
        for (AtomicStorableComponent<W, ?> element: field3.getAtomicComponents()) {
            elements.add(convertElement3(element, structure));
        }
        return StorableType.of(new StorableTypeDefinition<T>() {
            @Override
            public List<? extends AtomicStorableComponent<T, ?>> getAtomicComponents() {
                return elements;
            }

            @Override
            public T createInstance(ResultSet resultSet) throws SQLException {
                U element1 = field1.createInstance(resultSet);
                V element2 = field2.createInstance(resultSet);
                W element3 = field3.createInstance(resultSet);
                return structure.createValue(element1, element2, element3);
            }
        });
    }

}
