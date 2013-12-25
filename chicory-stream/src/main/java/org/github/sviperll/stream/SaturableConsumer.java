/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

import com.github.sviperll.Applicable;
import com.github.sviperll.Evaluatable;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
interface SaturableConsumer<T> extends SaturableConsuming<T> {
    SaturableConsumer<T> filtering(Evaluatable<? super T> predicate);
    SaturableConsumer<T> limiting(int limit);
    <U> SaturableConsumer<U> mapping(Applicable<U, ? extends T> function);
    SaturableConsumer<T> skipping(int offset);
}
