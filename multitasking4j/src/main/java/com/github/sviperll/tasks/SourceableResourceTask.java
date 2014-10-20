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
    public void perform() {
        source.provideResourceTo(new Consumer<TaskDefinition>() {
            @Override
            public void accept(TaskDefinition task) {
                currentTask = task;
                task.perform();
                currentTask = Task.doNothing();
            }
        });
    }

    @Override
    public void signalKill() {
        currentTask.signalKill();
    }

    @Override
    public void cleanup() {
        source.provideResourceTo(new Consumer<TaskDefinition>() {
            @Override
            public void accept(TaskDefinition task) {
                task.cleanup();
            }
        });
    }
}
