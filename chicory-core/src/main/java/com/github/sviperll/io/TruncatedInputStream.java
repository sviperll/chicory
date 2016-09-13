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
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TruncatedInputStream extends InputStream {
    public static InputStream createInstance(InputStream is, int contentLength) {
        if (contentLength >= 0)
            return new TruncatedInputStream(is, contentLength);
        else
            return is;
    }

    private final InputStream is;
    private int remained;

    public TruncatedInputStream(InputStream is, int length) {
        this.is = is;
        this.remained = length;
    }

    @Override
    public int available() throws IOException {
        return is.available();
    }

    @Override
    public void mark(int readlimit) {
        is.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return is.markSupported();
    }

    @Override
    public void reset() throws IOException {
        is.reset();
    }

    @Override
    public void close() throws IOException {
        is.close();
    }

    @Override
    public int read() throws IOException {
        if (remained == 0)
            return -1;
        else {
            int res = is.read();
            if (res >= 0)
                remained--;
            return res;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (remained <= 0 && len > 0)
            return -1;
        else {
            int effectiveLen = Math.min(remained, len);
            try {
                int bytesRead = is.read(b, off, effectiveLen);
                if (bytesRead > 0)
                    remained -= bytesRead;
                return bytesRead;
            } catch (IndexOutOfBoundsException ex) {
                Logger.getLogger(TruncatedInputStream.class.getName()).log(Level.INFO, "read(new byte[{0}], {1}, {2})", new Object[] {b.length, off, effectiveLen});
                throw ex;
            }
        }
    }

    @Override
    public long skip(long n) throws IOException {
        if (remained == 0 && n > 0)
            return 0;
        else {
            long effectiveN = Math.min(remained, n);
            long res = is.skip(effectiveN);
            if (res > 0)
                remained = (int)Math.max(0, remained - res);
            return res;
        }
    }
}
