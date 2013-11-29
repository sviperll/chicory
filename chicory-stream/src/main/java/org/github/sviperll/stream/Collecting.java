/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public interface Collecting<T, R, E extends Exception> extends SaturableConsuming<T>, ThrowingSupplier<R, E> {

}
