/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.repository;

/**
 *
 * @author vir
 */
public interface RepositoryIndex<K> extends RepositoryConfiguration {

    StorableTypeDefinition<K> getKeyDefinition();

}
