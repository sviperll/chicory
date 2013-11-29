/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.tasks;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TaskDefinition that logs when it's methods are called
 */
class LoggingTask implements TaskDefinition {
    private final String name;
    private final Logger logger;
    private final TaskDefinition task;
    
    /**
     * 
     * @param name name to use in log messages
     * @param logger logger to perform logging
     * @param task subtask that does actual work
     */
    public LoggingTask(String name, Logger logger, TaskDefinition task) {
        this.name = name;
        this.logger = logger;
        this.task = task;
    }

    @Override
    public void stop() {
        logger.log(Level.FINE, "{0}: exiting...", name);
        task.stop();
    }

    @Override
    public void run() {
        logger.log(Level.FINE, "{0}: started", name);
        task.run();
        logger.log(Level.FINE, "{0}: finished", name);
    }

    @Override
    public void close() {
        logger.log(Level.FINE, "{0}: closing...", name);
        task.close();
        logger.log(Level.FINE, "{0}: closed", name);
    }
}
