/*
 * Copyright (C) 2013 Victor Nazarov <asviraspossible@gmail.com>
 */

package com.github.sviperll.repository;

import com.github.sviperll.ClassStructure2;
import com.github.sviperll.ClassStructure3;
import com.github.sviperll.ClassStructure4;
import com.github.sviperll.IsomorphismDefinition;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StorableClasses {
    public static <T> StorableClass<T> create(final TableColumn<T> column) {
        return new StorableClass<T>() {
            @Override
            public List<? extends AtomicStorableClassComponent<T, ?>> getAtomicComponents() {
                return Collections.singletonList(new AtomicStorableClassComponent<T, T>() {
                    @Override
                    public TableColumn<T> getColumn() {
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
        };
    }

    public static <T, U> StorableClass<T> create(final TableColumn<U> column, final IsomorphismDefinition<T, U> structure) {
        return new StorableClass<T>() {
            @Override
            public List<? extends AtomicStorableClassComponent<T, ?>> getAtomicComponents() {
                return Collections.singletonList(new AtomicStorableClassComponent<T, U>() {
                    @Override
                    public TableColumn<U> getColumn() {
                        return column;
                    }

                    @Override
                    public U getComponent(T tuple) {
                        return structure.forward(tuple);
                    }
                });
            }

            @Override
            public T createInstance(ResultSet resultSet) throws SQLException {
                return structure.backward(column.retrieveValue(resultSet));
            }
        };
    }

    public static <T, U, V> StorableClass<T> create(final TableColumn<U> column1, final TableColumn<V> column2, final ClassStructure2<T, U, V> structure) {
        return new StorableClass<T>() {
            @Override
            public List<? extends AtomicStorableClassComponent<T, ?>> getAtomicComponents() {
                List<AtomicStorableClassComponent<T, ?>> list = new ArrayList<>();
                list.add(new AtomicStorableClassComponent<T, U>() {
                    @Override
                    public TableColumn<U> getColumn() {
                        return column1;
                    }

                    @Override
                    public U getComponent(T tuple) {
                        return structure.getField1(tuple);
                    }
                });
                list.add(new AtomicStorableClassComponent<T, V>() {
                    @Override
                    public TableColumn<V> getColumn() {
                        return column2;
                    }

                    @Override
                    public V getComponent(T tuple) {
                        return structure.getField2(tuple);
                    }
                });
                return list;
            }

            @Override
            public T createInstance(ResultSet resultSet) throws SQLException {
                return structure.createInstance(column1.retrieveValue(resultSet), column2.retrieveValue(resultSet));
            }
        };
    }

    public static <T, U, V, W> StorableClass<T> create(final TableColumn<U> column1, final TableColumn<V> column2, final TableColumn<W> column3, final ClassStructure3<T, U, V, W> structure) {
        return new StorableClass<T>() {
            @Override
            public List<? extends AtomicStorableClassComponent<T, ?>> getAtomicComponents() {
                List<AtomicStorableClassComponent<T, ?>> list = new ArrayList<>();
                list.add(new AtomicStorableClassComponent<T, U>() {
                    @Override
                    public TableColumn<U> getColumn() {
                        return column1;
                    }

                    @Override
                    public U getComponent(T tuple) {
                        return structure.getField1(tuple);
                    }
                });
                list.add(new AtomicStorableClassComponent<T, V>() {
                    @Override
                    public TableColumn<V> getColumn() {
                        return column2;
                    }

                    @Override
                    public V getComponent(T tuple) {
                        return structure.getField2(tuple);
                    }
                });
                list.add(new AtomicStorableClassComponent<T, W>() {
                    @Override
                    public TableColumn<W> getColumn() {
                        return column3;
                    }

                    @Override
                    public W getComponent(T tuple) {
                        return structure.getField3(tuple);
                    }
                });
                return list;
            }

            @Override
            public T createInstance(ResultSet resultSet) throws SQLException {
                U element1 = column1.retrieveValue(resultSet);
                V element2 = column2.retrieveValue(resultSet);
                W element3 = column3.retrieveValue(resultSet);
                return structure.createInstance(element1, element2, element3);
            }
        };
    }

    public static <T, U, V, W, X> StorableClass<T> create(final TableColumn<U> column1, final TableColumn<V> column2, final TableColumn<W> column3, final TableColumn<X> column4, final ClassStructure4<T, U, V, W, X> structure) {
        return new StorableClass<T>() {
            @Override
            public List<? extends AtomicStorableClassComponent<T, ?>> getAtomicComponents() {
                List<AtomicStorableClassComponent<T, ?>> list = new ArrayList<>();
                list.add(new AtomicStorableClassComponent<T, U>() {
                    @Override
                    public TableColumn<U> getColumn() {
                        return column1;
                    }

                    @Override
                    public U getComponent(T tuple) {
                        return structure.getField1(tuple);
                    }
                });
                list.add(new AtomicStorableClassComponent<T, V>() {
                    @Override
                    public TableColumn<V> getColumn() {
                        return column2;
                    }

                    @Override
                    public V getComponent(T tuple) {
                        return structure.getField2(tuple);
                    }
                });
                list.add(new AtomicStorableClassComponent<T, W>() {
                    @Override
                    public TableColumn<W> getColumn() {
                        return column3;
                    }

                    @Override
                    public W getComponent(T tuple) {
                        return structure.getField3(tuple);
                    }
                });
                list.add(new AtomicStorableClassComponent<T, X>() {
                    @Override
                    public TableColumn<X> getColumn() {
                        return column4;
                    }

                    @Override
                    public X getComponent(T tuple) {
                        return structure.getField4(tuple);
                    }
                });
                return list;
            }

            @Override
            public T createInstance(ResultSet resultSet) throws SQLException {
                U element1 = column1.retrieveValue(resultSet);
                V element2 = column2.retrieveValue(resultSet);
                W element3 = column3.retrieveValue(resultSet);
                X element4 = column4.retrieveValue(resultSet);
                return structure.createInstance(element1, element2, element3, element4);
            }
        };
    }

    public static <T, U> StorableClass<T> create(final StorableClass<U> baseTuple, final IsomorphismDefinition<T, U> structure) {
        final List<AtomicStorableClassComponent<T, ?>> elements = new ArrayList<>();
        for (AtomicStorableClassComponent<U, ?> element: baseTuple.getAtomicComponents()) {
            elements.add(convertElement(element, structure));
        }
        return new StorableClass<T>() {
            @Override
            public List<? extends AtomicStorableClassComponent<T, ?>> getAtomicComponents() {
                return elements;
            }

            @Override
            public T createInstance(ResultSet resultSet) throws SQLException {
                return structure.backward(baseTuple.createInstance(resultSet));
            }
        };
    }

    public static <T, U, V> StorableClass<T> create(final StorableClass<U> baseTuple1, final StorableClass<V> baseTuple2, final ClassStructure2<T, U, V> structure) {
        final List<AtomicStorableClassComponent<T, ?>> elements = new ArrayList<>();
        for (AtomicStorableClassComponent<U, ?> element: baseTuple1.getAtomicComponents()) {
            elements.add(convertElement1(element, structure));
        }
        for (AtomicStorableClassComponent<V, ?> element: baseTuple2.getAtomicComponents()) {
            elements.add(convertElement2(element, structure));
        }
        return new StorableClass<T>() {
            @Override
            public List<? extends AtomicStorableClassComponent<T, ?>> getAtomicComponents() {
                return elements;
            }

            @Override
            public T createInstance(ResultSet resultSet) throws SQLException {
                U element1 = baseTuple1.createInstance(resultSet);
                V element2 = baseTuple2.createInstance(resultSet);
                return structure.createInstance(element1, element2);
            }
        };
    }

    public static <T, U, V, W> StorableClass<T> create(final StorableClass<U> baseTuple1, final StorableClass<V> baseTuple2, final StorableClass<W> baseTuple3, final ClassStructure3<T, U, V, W> structure) {
        final List<AtomicStorableClassComponent<T, ?>> elements = new ArrayList<>();
        for (AtomicStorableClassComponent<U, ?> element: baseTuple1.getAtomicComponents()) {
            elements.add(convertElement1(element, structure));
        }
        for (AtomicStorableClassComponent<V, ?> element: baseTuple2.getAtomicComponents()) {
            elements.add(convertElement2(element, structure));
        }
        for (AtomicStorableClassComponent<W, ?> element: baseTuple3.getAtomicComponents()) {
            elements.add(convertElement3(element, structure));
        }
        return new StorableClass<T>() {
            @Override
            public List<? extends AtomicStorableClassComponent<T, ?>> getAtomicComponents() {
                return elements;
            }

            @Override
            public T createInstance(ResultSet resultSet) throws SQLException {
                U element1 = baseTuple1.createInstance(resultSet);
                V element2 = baseTuple2.createInstance(resultSet);
                W element3 = baseTuple3.createInstance(resultSet);
                return structure.createInstance(element1, element2, element3);
            }
        };
    }

    public static <T, U, V, W, X> StorableClass<T> create(final StorableClass<U> baseTuple1, final StorableClass<V> baseTuple2, final StorableClass<W> baseTuple3, final StorableClass<X> baseTuple4, final ClassStructure4<T, U, V, W, X> structure) {
        final List<AtomicStorableClassComponent<T, ?>> elements = new ArrayList<>();
        for (AtomicStorableClassComponent<U, ?> element: baseTuple1.getAtomicComponents())
            elements.add(convertElement1(element, structure));
        for (AtomicStorableClassComponent<V, ?> element: baseTuple2.getAtomicComponents())
            elements.add(convertElement2(element, structure));
        for (AtomicStorableClassComponent<W, ?> element: baseTuple3.getAtomicComponents())
            elements.add(convertElement3(element, structure));
        for (AtomicStorableClassComponent<X, ?> element: baseTuple4.getAtomicComponents())
            elements.add(convertElement4(element, structure));
        return new StorableClass<T>() {
            @Override
            public List<? extends AtomicStorableClassComponent<T, ?>> getAtomicComponents() {
                return elements;
            }

            @Override
            public T createInstance(ResultSet resultSet) throws SQLException {
                U element1 = baseTuple1.createInstance(resultSet);
                V element2 = baseTuple2.createInstance(resultSet);
                W element3 = baseTuple3.createInstance(resultSet);
                X element4 = baseTuple4.createInstance(resultSet);
                return structure.createInstance(element1, element2, element3, element4);
            }
        };
    }

    private static <T, U, V> AtomicStorableClassComponent<T, V> convertElement(final AtomicStorableClassComponent<U, V> element, final IsomorphismDefinition<T, U> structure) {
        return new AtomicStorableClassComponent<T, V>() {

            @Override
            public TableColumn<V> getColumn() {
                return element.getColumn();
            }

            @Override
            public V getComponent(T tuple) {
                return element.getComponent(structure.forward(tuple));
            }
        };
    }

    private static <T, U, V> AtomicStorableClassComponent<T, V> convertElement1(final AtomicStorableClassComponent<U, V> element, final ClassStructure2<T, U, ?> structure) {
        return new AtomicStorableClassComponent<T, V>() {

            @Override
            public TableColumn<V> getColumn() {
                return element.getColumn();
            }

            @Override
            public V getComponent(T tuple) {
                return element.getComponent(structure.getField1(tuple));
            }
        };
    }

    private static <T, U, V> AtomicStorableClassComponent<T, V> convertElement2(final AtomicStorableClassComponent<U, V> element, final ClassStructure2<T, ?, U> structure) {
        return new AtomicStorableClassComponent<T, V>() {

            @Override
            public TableColumn<V> getColumn() {
                return element.getColumn();
            }

            @Override
            public V getComponent(T tuple) {
                return element.getComponent(structure.getField2(tuple));
            }
        };
    }

    private static <T, U, V> AtomicStorableClassComponent<T, V> convertElement1(final AtomicStorableClassComponent<U, V> element, final ClassStructure3<T, U, ?, ?> structure) {
        return new AtomicStorableClassComponent<T, V>() {

            @Override
            public TableColumn<V> getColumn() {
                return element.getColumn();
            }

            @Override
            public V getComponent(T tuple) {
                return element.getComponent(structure.getField1(tuple));
            }
        };
    }

    private static <T, U, V> AtomicStorableClassComponent<T, V> convertElement2(final AtomicStorableClassComponent<U, V> element, final ClassStructure3<T, ?, U, ?> structure) {
        return new AtomicStorableClassComponent<T, V>() {

            @Override
            public TableColumn<V> getColumn() {
                return element.getColumn();
            }

            @Override
            public V getComponent(T tuple) {
                return element.getComponent(structure.getField2(tuple));
            }
        };
    }

    private static <T, U, V> AtomicStorableClassComponent<T, V> convertElement3(final AtomicStorableClassComponent<U, V> element, final ClassStructure3<T, ?, ?, U> structure) {
        return new AtomicStorableClassComponent<T, V>() {

            @Override
            public TableColumn<V> getColumn() {
                return element.getColumn();
            }

            @Override
            public V getComponent(T tuple) {
                return element.getComponent(structure.getField3(tuple));
            }
        };
    }

    private static <T, U, V> AtomicStorableClassComponent<T, V> convertElement1(final AtomicStorableClassComponent<U, V> element, final ClassStructure4<T, U, ?, ?, ?> structure) {
        return new AtomicStorableClassComponent<T, V>() {

            @Override
            public TableColumn<V> getColumn() {
                return element.getColumn();
            }

            @Override
            public V getComponent(T tuple) {
                return element.getComponent(structure.getField1(tuple));
            }
        };
    }

    private static <T, U, V> AtomicStorableClassComponent<T, V> convertElement2(final AtomicStorableClassComponent<U, V> element, final ClassStructure4<T, ?, U, ?, ?> structure) {
        return new AtomicStorableClassComponent<T, V>() {

            @Override
            public TableColumn<V> getColumn() {
                return element.getColumn();
            }

            @Override
            public V getComponent(T tuple) {
                return element.getComponent(structure.getField2(tuple));
            }
        };
    }

    private static <T, U, V> AtomicStorableClassComponent<T, V> convertElement3(final AtomicStorableClassComponent<U, V> element, final ClassStructure4<T, ?, ?, U, ?> structure) {
        return new AtomicStorableClassComponent<T, V>() {

            @Override
            public TableColumn<V> getColumn() {
                return element.getColumn();
            }

            @Override
            public V getComponent(T tuple) {
                return element.getComponent(structure.getField3(tuple));
            }
        };
    }

    private static <T, U, V> AtomicStorableClassComponent<T, V> convertElement4(final AtomicStorableClassComponent<U, V> element, final ClassStructure4<T, ?, ?, ?, U> structure) {
        return new AtomicStorableClassComponent<T, V>() {

            @Override
            public TableColumn<V> getColumn() {
                return element.getColumn();
            }

            @Override
            public V getComponent(T tuple) {
                return element.getComponent(structure.getField4(tuple));
            }
        };
    }

    private StorableClasses() {
    }
}
