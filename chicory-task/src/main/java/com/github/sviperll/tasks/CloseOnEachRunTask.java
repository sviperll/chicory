/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.tasks;

/**
 * This class represents TaskDefinition that behaves the same as a TaskDefinition passed to the constructor
 * but performes cleanup as the last action performed by run method.
 * <p>
 * #close method does nothing.
 * When #run method is called, #run method of the original task is called at first
 * and than #close method of the original task is called
 */
class CloseOnEachRunTask implements TaskDefinition {
    private final TaskDefinition task;
    
    /**
     * 
     * @param task original task to base behaviour on
     */
    public CloseOnEachRunTask(TaskDefinition task) {
        this.task = task;
    }

    /**
     * Calls #stop method of the original task @see TaskDefinition#stop
     */
    @Override
    public void stop() {
        task.stop();
    }

    /**
     * Does nothing
     */
    @Override
    public void close() {
    }

    /**
     * Calls #run method of the original task @see TaskDefinition#run
     * and than calls #close method of the original task @see TaskDefinition#close
     */
    @Override
    public void run() {
        try {
            task.run();
        } finally {
            task.close();
        }
    }
}
