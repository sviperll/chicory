/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.tasks;

/**
 * This class allows to dynamically change behaviour of the task
 * 
 * #set method can be used to change current behaviour
 */
public class DelegatingTask implements TaskDefinition {
    private volatile TaskDefinition task;

    /**
     * 
     * @param task initial behaviour of new instance
     */
    public DelegatingTask(TaskDefinition task) {
        this.task = task;
    }

    /**
     * Creates new instance with "doing nothing" initial behaviour
     * @see Task#doNothing()
     */
    public DelegatingTask() {
        this(new DoNothingTask());
    }

    /**
     * Changes current behaviour to new passed as a parameter
     * 
     * @param task new behaviour
     */
    public void set(TaskDefinition task) {
        this.task = task;
    }

    /**
     * runs #stop method of current behaviour, @see TaskDefinition#stop
     */
    @Override
    public void stop() {
        task.stop();
    }

    /**
     * runs #run method of current behaviour, @see TaskDefinition#run
     */
    @Override
    public void run() {
        task.run();
    }

    /**
     * runs #close method of current behaviour, @see TaskDefinition#close
     */
    @Override
    public void close() {
        task.close();
    }
}
