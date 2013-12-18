/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
interface DrainerResponseVisitor<T, R> {
    R fetched(T value);
    R closed();
    R error(RuntimeException exception);
}
