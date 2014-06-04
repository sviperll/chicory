/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.tasks;

import com.github.sviperll.Consumer;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public interface TaskGeneratorFactory {
    public TaskGenerator createTaskGenerator(Consumer<? super TaskDefinition> consumer);
}
