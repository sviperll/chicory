/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public interface SourceableResource<T> {
    public BindedConsumer bindConsumer(Consumer<? super T> consumer);
}
