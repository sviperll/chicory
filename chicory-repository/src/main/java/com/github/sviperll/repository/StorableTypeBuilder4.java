/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.repository;

import com.github.sviperll.TypeStructure4;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class StorableTypeBuilder4<T, U, V, W, X> {
    private static <T, U, V> AtomicStorableComponent<T, V> convertElement1(final AtomicStorableComponent<U, V> element, final TypeStructure4<T, U, ?, ?, ?> structure) {
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

    private static <T, U, V> AtomicStorableComponent<T, V> convertElement2(final AtomicStorableComponent<U, V> element, final TypeStructure4<T, ?, U, ?, ?> structure) {
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

    private static <T, U, V> AtomicStorableComponent<T, V> convertElement3(final AtomicStorableComponent<U, V> element, final TypeStructure4<T, ?, ?, U, ?> structure) {
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

    private static <T, U, V> AtomicStorableComponent<T, V> convertElement4(final AtomicStorableComponent<U, V> element, final TypeStructure4<T, ?, ?, ?, U> structure) {
        return new AtomicStorableComponent<T, V>() {

            @Override
            public TableColumnDefinition<V> getColumn() {
                return element.getColumn();
            }

            @Override
            public V getComponent(T tuple) {
                return element.getComponent(structure.getField4(tuple));
            }
        };
    }

    private final TypeStructure4<T, U, V, W, X> structure;

    public StorableTypeBuilder4(TypeStructure4<T, U, V, W, X> structure) {
        this.structure = structure;
    }

    public StorableType<T> build(final StorableTypeDefinition<U> field1, final StorableTypeDefinition<V> field2, final StorableTypeDefinition<W> field3, final StorableTypeDefinition<X> field4) {
        final List<AtomicStorableComponent<T, ?>> elements = new ArrayList<>();
        for (AtomicStorableComponent<U, ?> element: field1.getAtomicComponents())
            elements.add(convertElement1(element, structure));
        for (AtomicStorableComponent<V, ?> element: field2.getAtomicComponents())
            elements.add(convertElement2(element, structure));
        for (AtomicStorableComponent<W, ?> element: field3.getAtomicComponents())
            elements.add(convertElement3(element, structure));
        for (AtomicStorableComponent<X, ?> element: field4.getAtomicComponents())
            elements.add(convertElement4(element, structure));
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
                X element4 = field4.createInstance(resultSet);
                return structure.createValue(element1, element2, element3, element4);
            }
        });
    }

}
