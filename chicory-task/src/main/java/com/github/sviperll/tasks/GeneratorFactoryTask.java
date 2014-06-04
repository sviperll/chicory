/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.tasks;

import com.github.sviperll.Consumer;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class GeneratorFactoryTask implements TaskDefinition {
    private final TaskGeneratorFactory factory;
    private volatile TaskDefinition currentTask = Task.doNothing();

    public GeneratorFactoryTask(TaskGeneratorFactory factory) {
        this.factory = factory;
    }

    @Override
    public void run() {
        TaskGenerator generator = factory.createTaskGenerator(new Consumer<TaskDefinition>() {
            @Override
            public void accept(TaskDefinition task) {
                currentTask = task;
                task.run();
                currentTask = Task.doNothing();
            }
        });
        generator.passToConsumer();
    }

    @Override
    public void stop() {
        currentTask.stop();
    }

    @Override
    public void close() {
        TaskGenerator generator = factory.createTaskGenerator(new Consumer<TaskDefinition>() {
            @Override
            public void accept(TaskDefinition task) {
                task.close();
            }
        });
        generator.passToConsumer();
    }
}
