/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.repository;

import com.github.sviperll.OptionalVisitor;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author vir
 */
public class RepositoryFactory {
    private final RepositorySupport support;
    public RepositoryFactory(RepositorySupport support) {
        this.support = support;
    }

    public RepositorySupport support() {
        return support;
    }

    public <K, V> AutogeneratedKeyIndexedRepository<K, V> autogeneratedKeyIndexed(final AutogeneratedKeyIndexedRepositoryConfiguration<K, V> configuration) {
        return new AutogeneratedKeyIndexedRepository<K, V>() {
            @Override
            public K putNewEntry(V attributes) throws SQLException {
                return support.putNewEntry(configuration, attributes);
            }

            @Override
            public boolean putIfExists(K key, Changes<V> changes)
                    throws SQLException {
                return support.putIfExists(configuration, key, changes);
            }

            @Override
            public boolean remove(K key) throws SQLException {
                return support.remove(configuration, key);
            }

            @Override
            public <R, E extends Exception> R get(K key,
                                                  OptionalVisitor<V, R, E> optionalVisitor)
                    throws E, SQLException {
                return support.get(configuration, key, optionalVisitor);
            }
        };
    }

    public <K, V> AutogeneratedKeyIndexedRepository<K, V> autogeneratedKeyIndexed(final String tableName, final StorableTypeDefinition<K> key, final StorableTypeDefinition<K> autogeneratedKey, final StorableTypeDefinition<V> value) {
        return autogeneratedKeyIndexed(RepositoryConfigurations.autogeneratedKeyIndexed(tableName, key, autogeneratedKey, value));
    }

    public <K, V> IndexedRepository<K, V> indexed(final IndexedRepositoryConfiguration<K, V> configuration) {
        return new IndexedRepository<K, V>() {
            @Override
            public <R, E extends Exception> R get(K key,
                                                  OptionalVisitor<V, R, E> optionalVisitor) throws E, SQLException {
                return support.get(configuration, key, optionalVisitor);
            }

            @Override
            public boolean remove(K key) throws SQLException {
                return support.remove(configuration, key);
            }

            @Override
            public boolean putIfExists(K key, Changes<V> attributes) throws SQLException {
                return support.putIfExists(configuration, key, attributes);
            }

            @Override
            public void putNewEntry(K key, V attributes) throws SQLException {
                support.putNewEntry(configuration, key, attributes);
            }

            @Override
            public boolean put(K key, V attributes) throws SQLException {
                return support.put(configuration, key, attributes);
            }
        };
    }

    public <K, V> IndexedRepository<K, V> indexed(final String tableName, final StorableTypeDefinition<K> key, final StorableTypeDefinition<V> value) {
        return indexed(RepositoryConfigurations.indexed(tableName, key, value));
    }

    public <E, O> ReadableRepository<E, O> readable(final ReadableRepositoryConfiguration<E, O> configuration) {
        return new ReadableRepository<E, O>() {

            @Override
            public List<E> entryList(SlicingQuery<O> slicing) throws SQLException {
                return support.entryList(configuration, slicing);
            }
        };
    }

    public <E, O> ReadableRepository<E, O> readable(final String tableName, final StorableTypeDefinition<E> entry, final StorableTypeDefinition<O> ordering) {
        return readable(RepositoryConfigurations.readable(tableName, entry, ordering));
    }

    public <K, E, O> ReadableRepositoryDirectory<K, E, O> readableDirectory(final ReadableRepositoryDirectoryConfiguration<K, E, O> configuration) {
        return new ReadableRepositoryDirectory<K, E, O>() {
            @Override
            public ReadableRepository<E, O> get(final K key) throws SQLException {
                return new ReadableRepository<E, O>() {

                    @Override
                    public List<E> entryList(SlicingQuery<O> slicing)
                            throws SQLException {
                        return support.entryList(configuration, key, slicing);
                    }
                };
            }
        };
    }

    public <K, E, O> ReadableRepositoryDirectory<K, E, O> readableDirectory(final String tableName, final StorableTypeDefinition<E> entry, final StorableTypeDefinition<K> key, final StorableTypeDefinition<O> ordering) {
        return readableDirectory(RepositoryConfigurations.readableDirectory(tableName, entry, key, ordering));
    }

}
