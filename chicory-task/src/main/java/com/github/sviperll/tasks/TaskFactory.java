/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.tasks;

/**
 * Factory to build two tasks
 * <ol>
 * <li> Main task to perform actual work
 * <li> Closing task to perform cleanup for main task
 * </ol>
 */
public interface TaskFactory {
    /**
     * @return "main" task to perform actual work
     */
    TaskDefinition createWorkTask();

    /**
     * @return "closing" task to perform cleanup for "main" task
     */
    TaskDefinition createClosingTask();
}
