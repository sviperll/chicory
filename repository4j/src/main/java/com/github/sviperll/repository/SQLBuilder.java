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
package com.github.sviperll.repository;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

public class SQLBuilder implements Appendable, CharSequence {
    private final StringBuilder stringBuilder = new StringBuilder();
    private final SQLNameEscaping escaping;

    public SQLBuilder(SQLNameEscaping escaping) {
        this.escaping = escaping;
    }

    public SQLBuilder append(String s) {
        stringBuilder.append(s);
        return this;
    }

    public SQLBuilder appendJoinedTupleElements(String joinedWith, String format, List<? extends AtomicStorableComponent<?, ?>> elements) {
        Iterator<? extends AtomicStorableComponent<?, ?>> iterator = elements.iterator();
        if (iterator.hasNext()) {
            AtomicStorableComponent<?, ?> element = iterator.next();
            String columnName = element.getColumn().getColumnName();
            stringBuilder.append(MessageFormat.format(format, escaping.escapeName(columnName)));
            while (iterator.hasNext()) {
                element = iterator.next();
                columnName = element.getColumn().getColumnName();
                stringBuilder.append(joinedWith);
                stringBuilder.append(MessageFormat.format(format, escaping.escapeName(columnName)));
            }
        }
        return this;
    }

    public SQLBuilder appendJoined(String joinedWith, List<String> strings) {
        Iterator<String> iterator = strings.iterator();
        if (iterator.hasNext()) {
            String string = iterator.next();
            stringBuilder.append(string);
            while (iterator.hasNext()) {
                string = iterator.next();
                stringBuilder.append(joinedWith);
                stringBuilder.append(string);
            }
        }
        return this;
    }

    public <T> SQLBuilder appendConditionForColumn(SlicingQueryCondition<T> condition, AtomicStorableComponent<T, ?> element) {
        String columnName = element.getColumn().getColumnName();
        String op = condition.operator().sqlOperator();
        stringBuilder.append(escaping.escapeName(columnName)).append(" ").append(op).append(" ?");
        return this;
    }

    @Override
    public SQLBuilder append(CharSequence csq) throws IOException {
        stringBuilder.append(csq);
        return this;
    }

    @Override
    public SQLBuilder append(CharSequence csq, int start, int end) throws IOException {
        stringBuilder.append(csq, start, end);
        return this;
    }

    @Override
    public SQLBuilder append(char c) throws IOException {
        stringBuilder.append(c);
        return this;
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }

    @Override
    public int length() {
        return stringBuilder.length();
    }

    @Override
    public char charAt(int index) {
        return stringBuilder.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return stringBuilder.subSequence(start, end);
    }
}
