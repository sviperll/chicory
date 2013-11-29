/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.tasks;

/**
 * Run it's subtasks in sequence
 */
public class SequenceTask implements TaskDefinition {
    private final TaskDefinition[] tasks;
    /**
     * @param tasks Array of subtasks
     */
    public SequenceTask(TaskDefinition[] tasks) {
        this.tasks = tasks;
    }

    @Override
    public void stop() {
        RuntimeException exception = null;
        for (TaskDefinition task: tasks) {
            try {
                task.stop();
            } catch (RuntimeException ex) {
                exception = ex;
            }
        }
        if (exception != null)
            throw exception;
    }

    @Override
    public void run() {
        for (TaskDefinition task: tasks)
            task.run();
    }

    @Override
    public void close() {
        RuntimeException exception = null;
        for (TaskDefinition task: tasks) {
            try {
                task.close();
            } catch (RuntimeException ex) {
                exception = ex;
            }
        }
        if (exception != null)
            throw exception;
    }
}
