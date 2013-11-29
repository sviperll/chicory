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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class Base64 {
    /**
     * Encode byte sequence into another byte sequence according
     * to Base 64 encoding (RFC 1521, RFC 1421).
     * Each byte is either lating character, digit, plus, slash, equals sign,
     * line break ("\n") or cariage return ("\r") character in ASCII encoding.
     * Despite of specification, this function dosn't insert line breaks
     * if output line is longer than 76 characters.
     * You should use encodeIntoMultipleLines for that
     * @param bytes
     * @return encoded bytestream
     */
    public static byte[] encode(byte[] bytes) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStream os = createOneLineEncoderOutputStream(baos);
            try {
                os.write(bytes);
            } finally {
                os.close();
            }
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Encoding in memory shouldn't cause IO exception", ex);
        }
    }

    /**
     * Encode byte sequence into another byte sequence according
     * to Base 64 encoding (RFC 1521, RFC 1421).
     * Each byte is either lating character, digit, plus, slash, equals sign,
     * line break ("\n") or cariage return ("\r") character in ASCII encoding.
     * Output is automatically broken int multiple lines if it is too long.
     * This method is the same as encodeIntoMultipleLines(bytes, standardLineLength)
     * Where standardLineLength is maximum line length allowed by
     * MIME standard (RFC 1521), currently 76.

     * @param bytes
     * @return encoded bytestream
     */
    public static byte[] encodeIntoMultipleLines(byte[] bytes) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStream os = createEncoderOutputStream(baos);
            try {
                os.write(bytes);
            } finally {
                os.close();
            }
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Encoding in memory shouldn't cause IO exception", ex);
        }
    }

    /**
     * Encode byte sequence into another byte sequence according
     * to Base 64 encoding (RFC 1521, RFC 1421).
     * Each byte is either lating character, digit, plus, slash, equals sign,
     * line break ("\n") or cariage return ("\r") character in ASCII encoding.
     * Output is automatically broken int multiple lines if it is too long.
     *
     * @param bytes
     * @param splitLinesAt maximal length of lines in output
     * @return encoded bytestream
     */
    public static byte[] encodeIntoMultipleLines(byte[] bytes, int splitLinesAt) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStream os = createEncoderOutputStream(baos, splitLinesAt);
            try {
                os.write(bytes);
            } finally {
                os.close();
            }
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Encoding in memory shouldn't cause IO exception", ex);
        }
    }

    /**
     * Encode string into another string according
     * to Base 64 encoding (RFC 1521, RFC 1421).
     * Despite of specification, this function dosn't insert line breaks
     * if output line is longer than 76 characters.
     * You should use encodeIntoMultipleLines for that
     * @param s string to encode
     * @param charset string encoding to decode input string into sequence opf bytes
     * @return encoded string of lating characters, digits, plus, slash, equals signs,
     * line breaks ("\n") or cariage returns ("\r")
     */
    public static String encode(String s, Charset charset) {
        return new String(encode(s.getBytes(charset)), Charsets.ASCII);
    }

    /**
     * Encode string into another string according
     * to Base 64 encoding (RFC 1521, RFC 1421).
     * Despite of specification, this function dosn't insert line breaks
     * if output line is longer than 76 characters.
     * You should use encodeIntoMultipleLines for that
     * @param s string to encode
     * @param encoding string encoding to decode input string into sequence opf bytes
     * @return encoded string of lating characters, digits, plus, slash, equals signs,
     * line breaks ("\n") or cariage returns ("\r")
     * @throws UnsupportedEncodingException
     */
    public static String encode(String s, String encoding) throws UnsupportedEncodingException {
        return encode(s, Charset.forName(encoding));
    }

    /**
     * Encode string into another string according
     * to Base 64 encoding (RFC 1521, RFC 1421).
     * Output is automatically broken int multiple lines if it is too long.
     * Maximum line length is that allowed by
     * MIME standard (RFC 1521), currently 76.
     * @param s string to encode
     * @param charset string encoding to decode input string into sequence opf bytes
     * @return encoded string of lating characters, digits, plus, slash, equals signs,
     * line breaks ("\n") or cariage returns ("\r")
     */
    public static String encodeIntoMultipleLines(String s, Charset charset) {
        return new String(encodeIntoMultipleLines(s.getBytes(charset)), Charsets.ASCII);
    }

    /**
     * Encode string into another string according
     * to Base 64 encoding (RFC 1521, RFC 1421).
     * Output is automatically broken int multiple lines if it is too long.
     * Maximum line length is that allowed by
     * MIME standard (RFC 1521), currently 76.
     * @param s string to encode
     * @param encoding string encoding to decode input string into sequence opf bytes
     * @return encoded string of lating characters, digits, plus, slash, equals signs,
     * line breaks ("\n") or cariage returns ("\r")
     * @throws UnsupportedEncodingException 
     */
    public static String encodeIntoMultipleLines(String s, String encoding) throws UnsupportedEncodingException {
        return encodeIntoMultipleLines(s, Charset.forName(encoding));
    }


    /**
     * Create new OutputStream based on one passed as argument.
     * When bytes are written into resulting output stream they are
     * first converted into Base64 encoding (RFC 1521, RFC 1421) and
     * then written into ouput stream passed as argument.
     * Only lating characters, digits, plus, slash, equals signs,
     * line breaks ("\n") or cariage returns ("\r") in ASCII encoding
     * are written into argument output stream that way.
     * Despite of specification, this function dosn't insert line breaks
     * if output line is longer than 76 characters, you should use
     * createEncoderOutputStream for that.
     *
     * @param encoded output stream to write base64 encoded data into
     * @return output stream that encodes data into base64 and writes encoded data
     *         into output stream passed as argument
     */
    public static OutputStream createOneLineEncoderOutputStream(OutputStream encoded) {
        return new Base64OutputStream(encoded);
    }

    /**
     * Create new OutputStream based on one passed as argument.
     * When bytes are written into resulting output stream they are
     * first converted into Base64 encoding (RFC 1521, RFC 1421) and
     * then written into ouput stream passed as argument.
     * Only lating characters, digits, plus, slash, equals signs,
     * line breaks ("\n") or cariage returns ("\r") in ASCII encoding
     * are written into argument output stream that way.
     * Output is automatically broken int multiple lines if it is too long.
     * Maximum line length is that allowed by
     * MIME standard (RFC 1521), currently 76.
     *
     * @param encoded output stream to write base64 encoded data into
     * @return output stream that encodes data into base64 and writes encoded data
     *         into output stream passed as argument
     */
    public static OutputStream createEncoderOutputStream(OutputStream encoded) {
        OutputStream os = new LineSplitterOutputStream(encoded);
        return new Base64OutputStream(os);
    }


    /**
     * Create new OutputStream based on one passed as argument.
     * When bytes are written into resulting output stream they are
     * first converted into Base64 encoding (RFC 1521, RFC 1421) and
     * then written into ouput stream passed as argument.
     * Only lating characters, digits, plus, slash, equals signs,
     * line breaks ("\n") or cariage returns ("\r") in ASCII encoding
     * are written into argument output stream that way.
     * Output is automatically broken int multiple lines if it is too long.
     *
     * @param encoded output stream to write base64 encoded data into
     * @param splitLinesAt maximal line length of output lines
     * @return output stream that encodes data into base64 and writes encoded data
     *         into output stream passed as argument
     */
    public static OutputStream createEncoderOutputStream(OutputStream encoded, int splitLinesAt) {
        OutputStream os = new LineSplitterOutputStream(encoded, splitLinesAt);
        return new Base64OutputStream(os);
    }

    private Base64() {
    }
}
