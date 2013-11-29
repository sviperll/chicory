package com.github.sviperll.repository;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import com.github.sviperll.repository.SlicingQuery.SlicingQueryCondition;

class SQLBuilder implements Appendable, CharSequence {
    private final StringBuilder stringBuilder = new StringBuilder();

    public SQLBuilder append(String s) {
        stringBuilder.append(s);
        return this;
    }

    public SQLBuilder appendJoinedTupleElements(String joinedWith, String format, List<? extends AtomicStorableClassComponent<?, ?>> elements) {
        Iterator<? extends AtomicStorableClassComponent<?, ?>> iterator = elements.iterator();
        if (iterator.hasNext()) {
            AtomicStorableClassComponent<?, ?> element = iterator.next();
            String columnName = element.getColumn().getColumnName();
            stringBuilder.append(MessageFormat.format(format, columnName));
            while (iterator.hasNext()) {
                element = iterator.next();
                columnName = element.getColumn().getColumnName();
                stringBuilder.append(joinedWith);
                stringBuilder.append(MessageFormat.format(format, columnName));
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

    public <T> SQLBuilder appendConditionForColumn(SlicingQueryCondition<T> condition, AtomicStorableClassComponent<T, ?> element) {
        String columnName = element.getColumn().getColumnName();
        stringBuilder.append(columnName);
        if (condition.isLess()) {
            stringBuilder.append(" < ?");
        } else if (condition.isGreater()) {
            stringBuilder.append(" > ?");
        } else
            throw new IllegalStateException("Unsupported Repository condition: " + condition);
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
