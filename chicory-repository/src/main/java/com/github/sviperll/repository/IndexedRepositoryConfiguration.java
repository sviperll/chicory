/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.repository;

/**
 *
 * @author vir
 */
public interface IndexedRepositoryConfiguration<K, V> extends RepositoryIndex<K> {

    StorableClass<V> getValueDefinition();

}
