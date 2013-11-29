/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.tasks;

/**
 * This class represents TaskDefinition that behaves the same as a TaskDefinition passed to the constructor
 * but performes additinal cleanup when close method is called.
 * <p>
 * When #close method runs instance of this class calls #close method of original task
 * and than calls Runnable closingAction passed to the constructor
 */
class AdditionalClosingActionTask implements TaskDefinition {
    private final TaskDefinition task;
    private final Runnable closingAction;

    /**
     * 
     * @param task original task to base behaviour on
     * @param closingAction aditional action to perform when instance is closed
     */
    public AdditionalClosingActionTask(TaskDefinition task, Runnable closingAction) {
        this.task = task;
        this.closingAction = closingAction;
    }

    /**
     * Calls #run method of the original task @see TaskDefinition#run
     */
    @Override
    public void run() {
        task.run();
    }

    /**
     * Calls #stop method of the original task @see TaskDefinition#stop
     */
    @Override
    public void stop() {
        task.stop();
    }

    /**
     * Calls #close method of the original task (@see TaskDefinition#close)
     * and than calls additional closingAction
     */
    @Override
    public void close() {
        try {
            task.close();
        } finally {
            closingAction.run();
        }
    }
}
