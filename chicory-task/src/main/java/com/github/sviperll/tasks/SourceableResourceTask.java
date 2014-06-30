/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.tasks;

import com.github.sviperll.Consumer;
import com.github.sviperll.ResourceProviderDefinition;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class SourceableResourceTask implements TaskDefinition {
    private final ResourceProviderDefinition<? extends TaskDefinition> source;
    private volatile TaskDefinition currentTask = Task.doNothing();

    public SourceableResourceTask(ResourceProviderDefinition<? extends TaskDefinition> factory) {
        this.source = factory;
    }

    @Override
    public void run() {
        source.provideResourceTo(new Consumer<TaskDefinition>() {
            @Override
            public void accept(TaskDefinition task) {
                currentTask = task;
                task.run();
                currentTask = Task.doNothing();
            }
        });
    }

    @Override
    public void stop() {
        currentTask.stop();
    }

    @Override
    public void close() {
        source.provideResourceTo(new Consumer<TaskDefinition>() {
            @Override
            public void accept(TaskDefinition task) {
                task.close();
            }
        });
    }
}
