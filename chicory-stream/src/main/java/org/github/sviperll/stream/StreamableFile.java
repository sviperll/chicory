/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class StreamableFile {
    private final File file;
    public StreamableFile(File file) {
        this.file = file;
    }

    public File file() {
        return file;
    }

    public StreamableTextFile text(Charset charset) {
        return new StreamableTextFile(file, charset);
    }

    public Stream<Byte> bytes() {
        return Stream.of(new Streamable<Byte>() {
            @Override
            public void forEach(SaturableConsuming<? super Byte> consumer) {
                try {
                    try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                        int b;
                        while ((b = inputStream.read()) >= 0) {
                            if (!consumer.needsMore())
                                break;
                            consumer.accept((byte)b);
                        }
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
}
