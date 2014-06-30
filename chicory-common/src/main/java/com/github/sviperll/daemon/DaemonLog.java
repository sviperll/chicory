/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
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
import java.io.FileNotFoundException;
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
