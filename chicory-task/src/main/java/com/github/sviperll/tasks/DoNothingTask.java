/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.tasks;

/**
 * TaskDefinition that does nothing
 */
class DoNothingTask implements TaskDefinition {

    @Override
    public void stop() {
    }

    @Override
    public void run() {
    }

    @Override
    public void close() {
    }

}
