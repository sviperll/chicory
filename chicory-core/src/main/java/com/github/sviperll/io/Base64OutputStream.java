/*
 * Copyright (c) 2012, Victor Nazarov <asviraspossible@gmail.com>
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

import java.io.IOException;
import java.io.OutputStream;

public class Base64OutputStream extends OutputStream {
    private static final byte[] BASE64_CODES;
    static {
        StringBuilder sb = new StringBuilder();
        sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        sb.append("abcdefghijklmnopqrstuvwxyz");
        sb.append("0123456789");
        sb.append("+/");
        BASE64_CODES = sb.toString().getBytes(Charsets.ASCII);
    }

    private final byte[] buf = new byte[3];
    private final OutputStream encoded;

    private int bufOff = 0;
    Base64OutputStream(OutputStream encoded) {
        this.encoded = encoded;
    }

    @Override
    public void write(int b) throws IOException {
        buf[bufOff++] = (byte) b;
        if (bufOff == buf.length) {
            writeBuf();
            bufOff = 0;
        }
    }

    @Override
    public void flush() throws IOException {
        encoded.flush();
    }

    @Override
    public void close() throws IOException {
        if (bufOff != 0) {
            for (int i = bufOff; i < buf.length; i++)
                buf[i] = 0;
            int padding = buf.length - bufOff;
            writeBuf(4 - padding);
            for (int i = 0; i < padding; i++)
                encoded.write("=".getBytes(Charsets.ASCII));
        }
        encoded.close();
    }

    private void writeBuf() throws IOException {
        writeBuf(4);
    }
    private void writeBuf(int len) throws IOException {
        int j = ((buf[0] & 0xff) << 16) + ((buf[1] & 0xff) << 8) + (buf[2] & 0xff);
        for (int i = 0, shift = 18; i < len; i++, shift -= 6) {
            encoded.write(BASE64_CODES[(j >> shift) & 0x3f]);
        }
    }
}
