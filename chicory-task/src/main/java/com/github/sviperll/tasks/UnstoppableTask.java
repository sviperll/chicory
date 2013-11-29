/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.tasks;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class UnstoppableTask implements TaskDefinition {
    private final TaskDefinition task;

    public UnstoppableTask(TaskDefinition task) {
        this.task = task;
    }

    @Override
    public void run() {
        task.run();
    }

    @Override
    public void stop() {
    }

    @Override
    public void close() {
        task.close();
    }

}
