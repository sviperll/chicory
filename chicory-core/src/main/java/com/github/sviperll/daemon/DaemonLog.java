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

import com.github.sviperll.ResourceProvider;
import com.github.sviperll.ResourceProviderDefinition;
import com.github.sviperll.environment.HUPReopeningFileOutputStream;
import com.github.sviperll.logging.Handlers;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Handler;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class DaemonLog {
    private static final DaemonLog STANDARD_OUT =
            new DaemonLog(
                    ResourceProvider.forExisting(System.out).flatMap(DaemonLog::createFlushingHandler),
                    false,
                    false);
    private static final DaemonLog STANDARD_ERR =
            new DaemonLog(
                    ResourceProvider.forExisting(System.err).flatMap(DaemonLog::createFlushingHandler),
                    false,
                    false);

    public static DaemonLog standardOut() {
        return STANDARD_OUT;
    }

    public static DaemonLog standardErr() {
        return STANDARD_ERR;
    }

    public static DaemonLog createInstance(File file) {
        return createInstance(file, DaemonLog::createFlushingHandler);
    }

    private static ResourceProvider<Handler> createFlushingHandler(OutputStream stream) {
        return ResourceProvider.of(new FlushingHandlerResourceProvider(stream));
    }

    public static DaemonLog createInstance(File file, Function<? super OutputStream, ? extends ResourceProviderDefinition<? extends Handler>> loggingHandlerFactory) {
        ResourceProvider<OutputStream> streamProvider = ResourceProvider.of(new LogFileStreamProvider(file));
        ResourceProvider<? extends Handler> handlerProvider = streamProvider.flatMap(loggingHandlerFactory);
        return new DaemonLog(handlerProvider, true, true);
    }
    private final ResourceProviderDefinition<? extends Handler> handlerProvider;
    private final boolean closesStandardOut;
    private final boolean closesStandardErr;

    private DaemonLog(ResourceProviderDefinition<? extends Handler> handlerProvider, boolean closesStandardOut, boolean closesStandardErr) {
        this.handlerProvider = handlerProvider;
        this.closesStandardOut = closesStandardOut;
        this.closesStandardErr = closesStandardErr;
    }

    public ResourceProviderDefinition<? extends Handler> handlerProvider() {
        return handlerProvider;
    }

    boolean closesStandardOut() {
        return closesStandardOut;
    }

    boolean closesStandardErr() {
        return closesStandardErr;
    }

    private static class FlushingHandlerResourceProvider implements ResourceProviderDefinition<Handler> {

        private final OutputStream stream;

        FlushingHandlerResourceProvider(OutputStream stream) {
            this.stream = stream;
        }

        @Override
        public void provideResourceTo(Consumer<? super Handler> consumer) {
            Handler handler = Handlers.createFlushingHandler(stream);
            try {
                consumer.accept(handler);
            } finally {
                handler.close();
            }
        }
    }
    private static class LogFileStreamProvider implements ResourceProviderDefinition<OutputStream> {
        private final File file;

        LogFileStreamProvider(File file) {
            this.file = file;
        }

        @Override
        public void provideResourceTo(Consumer<? super OutputStream> consumer) {
            try (OutputStream stream = new HUPReopeningFileOutputStream(file);
                    BufferedOutputStream bufferedStream = new BufferedOutputStream(stream)) {

                consumer.accept(bufferedStream);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
