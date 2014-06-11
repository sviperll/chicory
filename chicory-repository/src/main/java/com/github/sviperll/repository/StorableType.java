/*
 * Copyright (C) 2013 Victor Nazarov <asviraspossible@gmail.com>
 */

package com.github.sviperll.repository;

import com.github.sviperll.TypeStructure2;
import com.github.sviperll.TypeStructure3;
import com.github.sviperll.TypeStructure4;
import com.github.sviperll.IsomorphismDefinition;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class StorableType<T> implements StorableTypeDefinition<T> {
    public static <T> StorableType<T> of(StorableTypeDefinition<T> definition) {
        if (definition instanceof StorableType)
            return (StorableType<T>)definition;
        else
            return new StorableType<>(definition);
    }

    public static <T> StorableType<T> of(final TableColumnDefinition<T> column) {
        return new StorableType<>(new StorableTypeDefinition<T>() {
            @Override
            public List<? extends AtomicStorableValueComponent<T, ?>> getAtomicComponents() {
                return Collections.singletonList(new AtomicStorableValueComponent<T, T>() {
                    @Override
                    public TableColumnDefinition<T> getColumn() {
                        return column;
                    }

                    @Override
                    public T getComponent(T tuple) {
                        return tuple;
                    }
                });
            }

            @Override
            public T createInstance(ResultSet resultSet) throws SQLException {
                return column.retrieveValue(resultSet);
            }
        });
    }

    public static <T, U> StorableType<T> of(final StorableTypeDefinition<U> base, final IsomorphismDefinition<T, U> isomorphism) {
        return of(isomorphism).build(base);
    }

    public static <T, U> StorableTypeBuilder1<T, U> of(IsomorphismDefinition<T, U> isomorphism) {
        return new StorableTypeBuilder1<>(isomorphism);
    }

    public static <T, U, V> StorableType<T> of(final StorableTypeDefinition<U> baseTuple1, final StorableTypeDefinition<V> baseTuple2, final TypeStructure2<T, U, V> structure) {
        return of(structure).build(baseTuple1, baseTuple2);
    }

    public static <T, U, V> StorableTypeBuilder2<T, U, V> of(TypeStructure2<T, U, V> structure) {
        return new StorableTypeBuilder2<>(structure);
    }

    public static <T, U, V, W> StorableType<T> of(final StorableTypeDefinition<U> baseTuple1, final StorableTypeDefinition<V> baseTuple2, final StorableTypeDefinition<W> baseTuple3, final TypeStructure3<T, U, V, W> structure) {
        return of(structure).build(baseTuple1, baseTuple2, baseTuple3);
    }

    public static <T, U, V, W> StorableTypeBuilder3<T, U, V, W> of(TypeStructure3<T, U, V, W> structure) {
        return new StorableTypeBuilder3<>(structure);
    }

    public static <T, U, V, W, X> StorableType<T> of(final StorableTypeDefinition<U> baseTuple1, final StorableTypeDefinition<V> baseTuple2, final StorableTypeDefinition<W> baseTuple3, final StorableTypeDefinition<X> baseTuple4, final TypeStructure4<T, U, V, W, X> structure) {
        return of(structure).build(baseTuple1, baseTuple2, baseTuple3, baseTuple4);
    }

    public static <T, U, V, W, X> StorableTypeBuilder4<T, U, V, W, X> of(TypeStructure4<T, U, V, W, X> structure) {
        return new StorableTypeBuilder4<>(structure);
    }


    private final StorableTypeDefinition<T> definition;

    private StorableType(StorableTypeDefinition<T> definition) {
        this.definition = definition;
    }

    @Override
    public List<? extends AtomicStorableValueComponent<T, ?>> getAtomicComponents() {
        return definition.getAtomicComponents();
    }

    @Override
    public T createInstance(ResultSet resultSet) throws SQLException {
        return definition.createInstance(resultSet);
    }

    public <U> StorableType<U> isomorphic(final IsomorphismDefinition<U, T> isomorphism) {
        return of(isomorphism).build(this);
    }
}
