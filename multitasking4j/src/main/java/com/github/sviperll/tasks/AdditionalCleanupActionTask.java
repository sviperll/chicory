/*
 * Copyright (c) 2014, Victor Nazarov <asviraspossible@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Victor Nazarov nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.sviperll.tasks;

/**
 * This class represents TaskDefinition that behaves the same as a TaskDefinition passed to the constructor
 * but performes additinal cleanup when cleanup method is called.
 * <p>
 * When #cleanup method runs instance of this class calls #cleanup method of original task
 * and than calls Runnable closingAction passed to the constructor
 */
class AdditionalCleanupActionTask implements TaskDefinition {
    private final TaskDefinition task;
    private final Runnable closingAction;

    /**
     * 
     * @param task original task to base behaviour on
     * @param closingAction aditional action to perform when instance is closed
     */
    public AdditionalCleanupActionTask(TaskDefinition task, Runnable closingAction) {
        this.task = task;
        this.closingAction = closingAction;
    }

    /**
     * Calls #perform method of the original task @see TaskDefinition#perform
     */
    @Override
    public void perform() {
        task.perform();
    }

    /**
     * Calls #signalKill method of the original task @see TaskDefinition#signalKill
     */
    @Override
    public void signalKill() {
        task.signalKill();
    }

    /**
     * Calls #cleanup method of the original task (@see TaskDefinition#cleanup)
     * and than calls additional closingAction
     */
    @Override
    public void cleanup() {
        try {
            task.cleanup();
        } finally {
            closingAction.run();
        }
    }
}
