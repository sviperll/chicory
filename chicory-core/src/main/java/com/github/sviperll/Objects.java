/*
 * Copyright (c) 2013, Victor Nazarov <asviraspossible@gmail.com>
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
package com.github.sviperll;

public class Objects {
    /**
     * @param constructorName
     * @param constructorArguments
     * @return string corresponding to Java's new expression to
     *         construct object
     *
     * <tt>{@code Objects.toString("ArrayList", 10)}</tt> has value
     * <tt>"new ArrayList(10)"</tt>
     */
    public static String toString(String constructorName, Object... constructorArguments) {
        SimpleStringBuilder ssb = new SimpleStringBuilder();
        ssb.append("new ");
        ssb.append(constructorName);
        ssb.append("(");
        if (constructorArguments.length > 0) {
            ssb.appendLiteral(constructorArguments[0]);
            for (int i = 1; i < constructorArguments.length; i++) {
                ssb.append(", ");
                ssb.appendLiteral(constructorArguments[i]);
            }
        }
        ssb.append(")");
        return ssb.toString();
    }

    public static boolean equals(int a, int b) {
        return a == b;
    }

    public static boolean equals(long a, long b) {
        return a == b;
    }

    public static boolean equals(byte a, byte b) {
        return a == b;
    }

    public static boolean equals(char a, char b) {
        return a == b;
    }

    public static boolean equals(float a, float b) {
        return a == b;
    }

    public static boolean equals(double a, double b) {
        return a == b;
    }

    /** Compare objects for equality
     * @param a
     * @param b
     * @return true if two objects are unique respecting null values
     */
    public static <T> boolean equals(T a, T b) {
        return a == b || (a != null && b != null && a.equals(b));
    }

    private Objects() {
    }

    private static class SimpleStringBuilder {
        private final StringBuilder sb = new StringBuilder();

        public void append(String s) {
            sb.append(s);
        }

        public void appendLiteral(Object literalValue) {
            if (literalValue != null && literalValue instanceof String) {
                sb.append(Strings.quote((String) literalValue));
            } else {
                sb.append(literalValue);
            }
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }
}
