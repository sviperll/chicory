/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.tasks;

import java.io.Closeable;

/**
 * Actions to perform
 */
public interface TaskDefinition extends Closeable {
    /**
     * Performs actual work associated with given task
     */
    void run();
    
    /**
     * Interrupts current work performed by task and abort any unfinished work
     */
    void stop();

    /**
     * Performs cleanup for given task, i.e. closes files and any other resources,
     * removes temporary files or database records, etc
     */
    @Override
    void close();
}
