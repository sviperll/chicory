/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.tasks;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TaskDefinition that catches all exceptions of the original task, logges them and then just suppresses them
 */
class ExceptionSwallowingTask implements TaskDefinition {
    private final TaskDefinition task;
    private final Logger logger;
    private final long pause;

    /**
     * 
     * @param task subtask to perform actual work
     * @param logger logger used to log exceptions
     * @param pause pause after exception
     */
    public ExceptionSwallowingTask(TaskDefinition task, Logger logger, long pause) {
        this.task = task;
        this.logger = logger;
        this.pause = pause;
    }

    @Override
    public void stop() {
        try {
            task.stop();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            try {
                Thread.sleep(pause);
            } catch (InterruptedException ex1) {
            }
        }
    }

    @Override
    public void run() {
        try {
            task.run();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            try {
                Thread.sleep(pause);
            } catch (InterruptedException ex1) {
            }
        }
    }

    @Override
    public void close() {
        try {
            task.close();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            try {
                Thread.sleep(pause);
            } catch (InterruptedException ex1) {
            }
        }
    }
}
