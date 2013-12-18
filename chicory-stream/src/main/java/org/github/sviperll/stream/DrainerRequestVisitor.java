/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
interface DrainerRequestVisitor<R> {
    R fetch();
    R close();
}
