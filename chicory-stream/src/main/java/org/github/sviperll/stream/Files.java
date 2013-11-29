/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

import java.io.File;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class Files {
    public static StreamableFile asStreamable(File file) {
        return new StreamableFile(file);
    }
    private Files() {
    }
}
