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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class TruncatedInputStreamTest {
    @Test
    public void testTruncationToEmpty() throws Exception {
        InputStream is = new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6});
        InputStream tr = new TruncatedInputStream(is, 0);
        byte[] res = readByteArray(tr);
        assertArrayEquals(new byte[] {}, res);
    }

    @Test
    public void testTruncationToOneByte() throws Exception {
        InputStream is = new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6});
        InputStream tr = new TruncatedInputStream(is, 1);
        byte[] res = readByteArray(tr);
        assertArrayEquals(new byte[] {1}, res);
    }

    @Test
    public void testTruncationToOneByteLessThenRealLength() throws Exception {
        InputStream is = new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6});
        InputStream tr = new TruncatedInputStream(is, 5);
        byte[] res = readByteArray(tr);
        assertArrayEquals(new byte[] {1, 2, 3, 4, 5}, res);
    }

    @Test
    public void testTruncationToRealLength() throws Exception {
        InputStream is = new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6});
        InputStream tr = new TruncatedInputStream(is, 6);
        byte[] res = readByteArray(tr);
        assertArrayEquals(new byte[] {1, 2, 3, 4, 5, 6}, res);
    }

    @Test
    public void testTruncationToOneByteMoreThenRealLength() throws Exception {
        InputStream is = new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6});
        InputStream tr = new TruncatedInputStream(is, 7);
        byte[] res = readByteArray(tr);
        assertArrayEquals(new byte[] {1, 2, 3, 4, 5, 6}, res);
    }

    @Test
    public void testSingleCharacterRead() throws Exception {
        InputStream is = new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6});
        InputStream tr = new TruncatedInputStream(is, 3);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        while ((b = tr.read()) != -1) {
            baos.write(b);
        }
        assertArrayEquals(new byte[] {1, 2, 3}, baos.toByteArray());
    }

    @Test
    public void testWholeBufferRead() throws Exception {
        InputStream is = new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6});
        InputStream tr = new TruncatedInputStream(is, 4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[2];
        int len;
        while ((len = tr.read(buf)) != -1)
            baos.write(buf, 0, len);
        assertArrayEquals(new byte[] {1, 2, 3, 4}, baos.toByteArray());
    }


    @Test
    public void testBufferSegmentRead() throws Exception {
        InputStream is = new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6});
        InputStream tr = new TruncatedInputStream(is, 4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4];
        int len;
        while ((len = tr.read(buf, 2, 2)) != -1)
            baos.write(buf, 2, len);
        assertArrayEquals(new byte[] {1, 2, 3, 4}, baos.toByteArray());
    }

    private byte[] readByteArray(InputStream tr) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        while ((b = tr.read()) != -1) {
            baos.write(b);
        }
        return baos.toByteArray();
    }
}