/*
 * Copyright (C) 2013 Victor Nazarov <asviraspossible@gmail.com>
 */

package com.github.sviperll;

public interface OptionalVisitor<T, R, E extends Exception> {
    R present(T value) throws E;
    R missing() throws E;
}
