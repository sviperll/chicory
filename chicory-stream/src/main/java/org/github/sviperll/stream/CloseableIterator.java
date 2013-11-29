/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

import java.io.Closeable;
import java.util.Iterator;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public interface CloseableIterator<T> extends Iterator<T>, Closeable {

}
