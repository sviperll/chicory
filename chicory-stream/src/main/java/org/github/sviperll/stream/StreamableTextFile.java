/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class StreamableTextFile {
    private final File file;
    private final Charset charset;

    public StreamableTextFile(File file, Charset charset) {
        this.file = file;
        this.charset = charset;
    }

    public File file() {
        return file;
    }

    public StreamableFile binary() {
        return new StreamableFile(file);
    }

    public Stream<String> lines() {
        return Stream.of(new Streamable<String>() {
            @Override
            public void forEach(SaturableConsuming<? super String> consumer) {
                try {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), charset))) {
                        String s;
                        while ((s = reader.readLine()) != null) {
                            if (!consumer.needsMore())
                                break;
                            consumer.accept(s);
                        }
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    public Stream<Character> characters() {
        return Stream.of(new Streamable<Character>() {
            @Override
            public void forEach(SaturableConsuming<? super Character> consumer) {
                try {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), charset))) {
                        int c;
                        while ((c = reader.read()) >= 0) {
                            if (!consumer.needsMore())
                                break;
                            consumer.accept((char)c);
                        }
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
}
