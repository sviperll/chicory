/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.repository;

import com.github.sviperll.OptionalVisitor;
import java.sql.SQLException;

/**
 *
 * @author vir
 */
public interface IndexedRepository<K, V> {

    <R, E extends Exception> R get(K key,
                                   OptionalVisitor<V, R, E> optionalVisitor)
            throws E, SQLException;

    boolean remove(K key) throws SQLException;

    boolean putIfExists(K key, Changed<V> attributes) throws SQLException;

    void putNewEntry(K key, V attributes) throws SQLException;

    boolean put(K key, V attributes) throws SQLException;

}
