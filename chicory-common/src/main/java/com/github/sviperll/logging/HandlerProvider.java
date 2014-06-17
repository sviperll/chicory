/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.logging;

import com.github.sviperll.Consumer;
import java.util.logging.Handler;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public interface HandlerProvider {
    void provideHandler(Consumer<? super Handler> consumer);
}
