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

    public StorableClass<V> getEntryDefinition();

    public StorableClass<O> getOrderingDefinition();

}
