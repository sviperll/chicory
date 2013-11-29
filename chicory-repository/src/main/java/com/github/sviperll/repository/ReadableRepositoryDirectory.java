/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.repository;

import java.sql.SQLException;

/**
 *
 * @author vir
 */
public interface ReadableRepositoryDirectory<K, E, O> {
    ReadableRepository<E, O> get(K key) throws SQLException;
}
