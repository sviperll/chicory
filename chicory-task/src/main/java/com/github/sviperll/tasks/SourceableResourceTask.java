/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.tasks;

import com.github.sviperll.BindedConsumer;
import com.github.sviperll.Consumer;
import com.github.sviperll.SourceableResource;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class SourceableResourceTask implements TaskDefinition {
    private final SourceableResource<? extends TaskDefinition> source;
    private volatile TaskDefinition currentTask = Task.doNothing();

    public SourceableResourceTask(SourceableResource<? extends TaskDefinition> factory) {
        this.source = factory;
    }

    @Override
    public void run() {
        BindedConsumer bindedConsumer = source.bindConsumer(new Consumer<TaskDefinition>() {
            @Override
            public void accept(TaskDefinition task) {
                currentTask = task;
                task.run();
                currentTask = Task.doNothing();
            }
        });
        bindedConsumer.acceptProvidedValue();
    }

    @Override
    public void stop() {
        currentTask.stop();
    }

    @Override
    public void close() {
        BindedConsumer bindedConsumer = source.bindConsumer(new Consumer<TaskDefinition>() {
            @Override
            public void accept(TaskDefinition task) {
                task.close();
            }
        });
        bindedConsumer.acceptProvidedValue();
    }
}
