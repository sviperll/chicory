/*
 * Copyright (C) 2013 Victor Nazarov <asviraspossible@gmail.com>
 */

package com.github.sviperll.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class Files {
    public static String read(File passwordFile, Charset charset) throws FileNotFoundException, IOException {
        try (InputStream stream = new BufferedInputStream(new FileInputStream(passwordFile))) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[2048];
            int bytesRead;
            while ((bytesRead = reader.read(buffer)) >= 0)
                builder.append(buffer, 0, bytesRead);
            return builder.toString();
        }
    }

    public static void write(File file, String contents, Charset charset) throws IOException {
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset));
            try {
                writer.write(contents);
            } finally {
                writer.flush();
            }
        }
    }

    public static String nameWithoutExtention(String fileName) {
        int baseNameStart = fileName.lastIndexOf(File.separatorChar) + 1;
        int suffixStart = fileName.lastIndexOf('.');
        String nameWithoutExtention = fileName;
        if (suffixStart > 0 && suffixStart > baseNameStart)
            nameWithoutExtention = fileName.substring(0, suffixStart);
        return nameWithoutExtention;
    }

    /**
     *
     * @param fileName filename with extention
     * @return extention with leading dot
     */
    public static String extention(String fileName) {
        int baseNameStart = fileName.lastIndexOf(File.separatorChar) + 1;
        int suffixStart = fileName.lastIndexOf('.');
        String extention = null;
        if (suffixStart > 0 && suffixStart > baseNameStart)
            extention = fileName.substring(0, suffixStart);
        return extention;
    }

    public static String nameWithExtention(String fileName, String extention) {
        if (extention.startsWith("."))
            return fileName + extention;
        else
            return fileName + "." + extention;
    }

    private Files() {
    }
}
