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
