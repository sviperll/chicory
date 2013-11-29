/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.tasks;


/**
 * TaskDefinition that uses factory to build two sub-tasks:
 * 
 * <ol>
 * <li>Main task and
 * <li>Cleanup task
 * </ol>
 * 
 * Main task is used in #run method.
 * Cleanup task is used in #close method.
 */
class FactoryTask implements TaskDefinition {
    private final TaskFactory factory;
    private volatile TaskDefinition task = Task.doNothing();
    
    /**
     * 
     * @param factory factory to create main task and cleanup task
     */
    public FactoryTask(TaskFactory factory) {
        this.factory = factory;
    }

    @Override
    public void stop() {
        task.stop();
    }

    /**
     * Creates "main" task, calls it's #run and then calls it's #close method
     * @see TaskDefinition#run
     * @see TaskDefinition#close
     */
    @Override
    public void run() {
        task = factory.createWorkTask();
        try {
            task.run();
        } finally {
            task.close();
            task = Task.doNothing();
        }
    }

    /**
     * Creates "cleanup" task, calls it's #run and then calls it's #close method
     * @see TaskDefinition#run
     * @see TaskDefinition#close
     */
    @Override
    public void close() {
        try (TaskDefinition unstoppableTask = factory.createClosingTask()) {
            unstoppableTask.run();
        }
    }
}
