/*
 * Copyright (c) 2014, Victor Nazarov <asviraspossible@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Victor Nazarov nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.sviperll.repository;

import com.github.sviperll.IsomorphismDefinition;
import com.github.sviperll.TypeStructure2;
import com.github.sviperll.TypeStructure3;
import com.github.sviperll.TypeStructure4;
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

    public static <T> StorableType<T> of(final ColumnStorableTypeBuilderDefinition<T> column) {
        return new StorableType<>(new StorableTypeDefinition<T>() {
            @Override
            public List<? extends AtomicStorableComponent<T, ?>> getAtomicComponents() {
                return Collections.singletonList(new AtomicStorableComponent<T, T>() {
                    @Override
                    public ColumnStorableTypeBuilderDefinition<T> getColumn() {
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

    public static ColumnBuilder forColumn(String name) {
        return new ColumnBuilder(name);
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
    public List<? extends AtomicStorableComponent<T, ?>> getAtomicComponents() {
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
