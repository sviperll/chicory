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
public interface AutogeneratedKeyIndexedRepository<K, V> {

    K putNewEntry(V attributes) throws SQLException;

    boolean putIfExists(K key, Changes<V> attributes) throws SQLException;

    boolean remove(K key) throws SQLException;

    <R, E extends Exception> R get(K key,
                                   OptionalVisitor<V, R, E> optionalVisitor)
            throws E, SQLException;

}
