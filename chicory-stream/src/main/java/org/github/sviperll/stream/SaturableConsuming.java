/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public interface SaturableConsuming<T> {
    /**
     * Accept value for processing.
     * Can be called more than once.
     * Can be called event if #needsMore method returns false
     * @param value
     */
    void accept(T value);

    /**
     * Shows if consumer expects more values to process or
     * consumer is saturated and need no more values
     * @return true if consumer expects more values.
     */
    boolean needsMore();
}
