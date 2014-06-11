/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.repository;

/**
 *
 * @author vir
 */
public interface ReadableRepositoryConfiguration<V, O> extends RepositoryConfiguration {

    public StorableTypeDefinition<V> getEntryDefinition();

    public StorableTypeDefinition<O> getOrderingDefinition();

}
