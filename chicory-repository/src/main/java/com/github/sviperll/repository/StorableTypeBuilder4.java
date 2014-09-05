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
            public ColumnStorableTypeBuilderDefinition<V> getColumn() {
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
            public ColumnStorableTypeBuilderDefinition<V> getColumn() {
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
            public ColumnStorableTypeBuilderDefinition<V> getColumn() {
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
            public ColumnStorableTypeBuilderDefinition<V> getColumn() {
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
        final List<AtomicStorableComponent<T, ?>> elements = new ArrayList<AtomicStorableComponent<T, ?>>();
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
