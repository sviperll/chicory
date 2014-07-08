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
package com.github.sviperll.daemon;

import com.github.sviperll.Applicable;
import com.github.sviperll.Consumer;
import com.github.sviperll.ResourceProvider;
import com.github.sviperll.ResourceProviderDefinition;
import com.github.sviperll.environment.HUPReopeningFileOutputStream;
import com.github.sviperll.logging.Loggers;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Handler;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class DaemonLog {
    public static DaemonLog createInstance(File file) {
        return createInstance(file, new FlushingLoggingFactory());
    }

    public static DaemonLog createInstance(File file, Applicable<? super OutputStream, ? extends ResourceProviderDefinition<? extends Handler>> loggingHandlerFactory) {
        ResourceProvider<OutputStream> streamProvider = ResourceProvider.of(new LogFileStreamProvider(file));
        ResourceProvider<? extends Handler> handlerProvider = streamProvider.flatMap(loggingHandlerFactory);
        return new DaemonLog(handlerProvider);
    }
    private final ResourceProvider<? extends Handler> handlerProvider;

    private DaemonLog(ResourceProvider<? extends Handler> handlerProvider) {
        this.handlerProvider = handlerProvider;
    }

    public ResourceProvider<? extends Handler> handlerProvider() {
        return handlerProvider;
    }

    private static class FlushingLoggingFactory implements Applicable<OutputStream, ResourceProvider<Handler>> {
        @Override
        public ResourceProvider<Handler> apply(final OutputStream stream) {
            return ResourceProvider.of(new ResourceProviderDefinition<Handler>() {
                @Override
                public void provideResourceTo(Consumer<? super Handler> consumer) {
                    Handler handler = Loggers.createFlushingHandler(stream);
                    try {
                        consumer.accept(handler);
                    } finally {
                        handler.close();
                    }
                }
            });
        }
    }

    private static class LogFileStreamProvider implements ResourceProviderDefinition<OutputStream> {
        private final File file;
        LogFileStreamProvider(File file) {
            this.file = file;
        }
        @Override
        public void provideResourceTo(Consumer<? super OutputStream> consumer) {
            try {
                try (OutputStream stream = new HUPReopeningFileOutputStream(file);
                     BufferedOutputStream bufferedStream = new BufferedOutputStream(stream)) {
                    consumer.accept(bufferedStream);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
