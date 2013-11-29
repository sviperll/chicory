/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.tasks;

/**
 *
 * @author vir
 */
class WithoutCloseTask implements TaskDefinition {
    private final TaskDefinition task;

    public WithoutCloseTask(TaskDefinition task) {
        this.task = task;
    }

    @Override
    public void run() {
        task.run();
    }

    @Override
    public void stop() {
        task.stop();
    }

    @Override
    public void close() {
    }
    
}
