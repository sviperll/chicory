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

import com.github.sviperll.IsomorphismDefinition;
import com.github.sviperll.repository.ColumnStorableTypeBuilderDefinition.PreparedStatementParameterSetter;
import com.github.sviperll.time.Time;
import com.github.sviperll.time.UnixTime;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class ColumnStorableTypeBuilder<T> implements ColumnStorableTypeBuilderDefinition<T> {
    /**
     * You should probably use {@link StorableType#forColumn}
     *
     * @see StorableType#forColumn
     */
    @Deprecated
    public static ColumnStorableTypeBuilder<Integer> integer(String columnName) {
        return integerColumnBuilder(columnName);
    }

    /**
     * You should probably use {@link StorableType#forColumn}
     *
     * @see StorableType#forColumn
     */
    @Deprecated
    public static ColumnStorableTypeBuilder<String> string(String columnName) {
        return stringColumnBuilder(columnName);
    }

    /**
     * You should probably use {@link StorableType#forColumn}
     *
     * @see StorableType#forColumn
     */
    @Deprecated
    public static ColumnStorableTypeBuilder<UnixTime> unixTime(String columnName) {
        return unixTimeColumnBuilder(columnName);
    }

    /**
     * You should probably use {@link StorableType#forColumn}
     *
     * @see StorableType#forColumn
     */
    @Deprecated
    public static ColumnStorableTypeBuilder<Boolean> booleanColumn(final String columnName) {
        return booleanColumnBuilder(columnName);
    }

    static ColumnStorableTypeBuilder<Integer> integerColumnBuilder(String columnName) {
        return new ColumnStorableTypeBuilder<Integer>(new IntegerColumn(columnName));
    }

    static ColumnStorableTypeBuilder<String> stringColumnBuilder(String columnName) {
        return new ColumnStorableTypeBuilder<String>(new StringColumn(columnName));
    }

    static ColumnStorableTypeBuilder<UnixTime> unixTimeColumnBuilder(String columnName) {
        return new ColumnStorableTypeBuilder<UnixTime>(new UnixTimeColumn(columnName));
    }

    static ColumnStorableTypeBuilder<Boolean> booleanColumnBuilder(final String columnName) {
        return new ColumnStorableTypeBuilder<Boolean>(new BooleanColumn(columnName));
    }

    public static <T> ColumnStorableTypeBuilder<T> of(ColumnStorableTypeBuilderDefinition<T> column) {
        if (column instanceof ColumnStorableTypeBuilder)
            return (ColumnStorableTypeBuilder<T>)column;
        else
            return new ColumnStorableTypeBuilder<T>(column);
    }

    private final ColumnStorableTypeBuilderDefinition<T> column;
    private ColumnStorableTypeBuilder(ColumnStorableTypeBuilderDefinition<T> column) {
        this.column = column;
    }

    @Override
    public String getColumnName() {
        return column.getColumnName();
    }

    @Override
    public PreparedStatementParameterSetter<T> createStatementSetter(PreparedStatement statement) {
        return column.createStatementSetter(statement);
    }

    @Override
    public T retrieveValue(ResultSet resultSet) throws SQLException {
        return column.retrieveValue(resultSet);
    }

    @Override
    public T retrieveValue(ResultSet resultSet, String label) throws SQLException {
        return column.retrieveValue(resultSet, label);
    }

    @Override
    public T retrieveValue(ResultSet resultSet, int index) throws SQLException {
        return column.retrieveValue(resultSet, index);
    }

    public <U> ColumnStorableTypeBuilder<U> isomorphic(IsomorphismDefinition<U, T> structure) {
        return new ColumnStorableTypeBuilder<U>(new IsomorphicColumn<U, T>(column, structure));
    }

    public ColumnStorableTypeBuilder<T> retrievedByIndex(int index) {
        return new ColumnStorableTypeBuilder<T>(new RetrievedByIndexColumn<T>(column, index));
    }

    /**
     * @return storable type for single column
     */
    public StorableType<T> build() {
        return StorableType.of(column);
    }

    private static class IntegerColumn implements ColumnStorableTypeBuilderDefinition<Integer> {
        private final String name;
        public IntegerColumn(String name) {
            this.name = name;
        }

        @Override
        public String getColumnName() {
            return name;
        }

        @Override
        public PreparedStatementParameterSetter<Integer> createStatementSetter(final PreparedStatement statement) {
            return new IntegerColumnStatementSetter(statement);
        }

        @Override
        public Integer retrieveValue(ResultSet resultSet) throws SQLException {
            return retrieveValue(resultSet, name);
        }

        @Override
        public Integer retrieveValue(ResultSet resultSet, String label) throws SQLException {
            int value = resultSet.getInt(label);
            if (resultSet.wasNull())
                return null;
            else
                return value;
        }

        @Override
        public Integer retrieveValue(ResultSet resultSet, int index) throws SQLException {
            int value = resultSet.getInt(index);
            if (resultSet.wasNull())
                return null;
            else
                return value;
        }

        private static class IntegerColumnStatementSetter implements PreparedStatementParameterSetter<Integer> {
            private final PreparedStatement statement;

            public IntegerColumnStatementSetter(PreparedStatement statement) {
                this.statement = statement;
            }

            @Override
            public void setValue(int index, Integer value) throws SQLException {
                if (value == null)
                    statement.setNull(index, java.sql.Types.INTEGER);
                else
                    statement.setInt(index, value);
            }
        }

    }

    private static class StringColumn implements ColumnStorableTypeBuilderDefinition<String> {
        private final String name;
        public StringColumn(String name) {
            this.name = name;
        }

        @Override
        public String getColumnName() {
            return name;
        }

        @Override
        public PreparedStatementParameterSetter<String> createStatementSetter(final PreparedStatement statement) {
            return new StringColumnStatementSetter(statement);
        }

        @Override
        public String retrieveValue(ResultSet resultSet) throws SQLException {
            return retrieveValue(resultSet, name);
        }

        @Override
        public String retrieveValue(ResultSet resultSet, String label) throws SQLException {
            return resultSet.getString(label);
        }

        @Override
        public String retrieveValue(ResultSet resultSet, int index) throws SQLException {
            return resultSet.getString(index);
        }

        private static class StringColumnStatementSetter implements PreparedStatementParameterSetter<String> {
            private final PreparedStatement statement;

            public StringColumnStatementSetter(PreparedStatement statement) {
                this.statement = statement;
            }

            @Override
            public void setValue(int index, String value) throws SQLException {
                if (value == null)
                    statement.setNull(index, java.sql.Types.VARCHAR);
                else
                    statement.setString(index, value);
            }
        }
    }

    private static class UnixTimeColumn implements ColumnStorableTypeBuilderDefinition<UnixTime> {
        private final String name;
        public UnixTimeColumn(String name) {
            this.name = name;
        }

        @Override
        public String getColumnName() {
            return name;
        }

        @Override
        public PreparedStatementParameterSetter<UnixTime> createStatementSetter(final PreparedStatement statement) {
            return new UnixTimeColumnStatementSetter(statement);
        }

        @Override
        public UnixTime retrieveValue(ResultSet resultSet) throws SQLException {
            return retrieveValue(resultSet, name);
        }

        @Override
        public UnixTime retrieveValue(ResultSet resultSet, String label) throws SQLException {
            return Time.resultSet(resultSet).getUnixTime(label, Time.GMT_OFFSET);
        }

        @Override
        public UnixTime retrieveValue(ResultSet resultSet, int index) throws SQLException {
            return Time.resultSet(resultSet).getUnixTime(index, Time.GMT_OFFSET);
        }

        private static class UnixTimeColumnStatementSetter implements PreparedStatementParameterSetter<UnixTime> {
            private final PreparedStatement statement;

            public UnixTimeColumnStatementSetter(PreparedStatement statement) {
                this.statement = statement;
            }

            @Override
            public void setValue(int index, UnixTime value) throws SQLException {
                Time.preparedStatement(statement).setUnixTime(index, value, Time.GMT_OFFSET);
            }
        }
    }

    private static class IsomorphicColumn<T, U> implements ColumnStorableTypeBuilderDefinition<T> {
        private final ColumnStorableTypeBuilderDefinition<U> columnDefinition;
        private final IsomorphismDefinition<T, U> structure;
        public IsomorphicColumn(ColumnStorableTypeBuilderDefinition<U> columnDefinition, IsomorphismDefinition<T, U> structure) {
            this.columnDefinition = columnDefinition;
            this.structure = structure;
        }

        @Override
        public String getColumnName() {
            return columnDefinition.getColumnName();
        }

        @Override
        public PreparedStatementParameterSetter<T> createStatementSetter(PreparedStatement statement) {
            final PreparedStatementParameterSetter<U> setter = columnDefinition.createStatementSetter(statement);
            return new PreparedStatementParameterSetter<T>() {
                @Override
                public void setValue(int index, T value) throws SQLException {
                    setter.setValue(index, structure.forward(value));
                }
            };
        }

        @Override
        public T retrieveValue(ResultSet resultSet) throws SQLException {
            return structure.backward(columnDefinition.retrieveValue(resultSet));
        }

        @Override
        public T retrieveValue(ResultSet resultSet, String label) throws SQLException {
            return structure.backward(columnDefinition.retrieveValue(resultSet, label));
        }

        @Override
        public T retrieveValue(ResultSet resultSet, int index) throws SQLException {
            return structure.backward(columnDefinition.retrieveValue(resultSet, index));
        }
    }

    private static class RetrievedByIndexColumn<T> implements ColumnStorableTypeBuilderDefinition<T> {
        private final ColumnStorableTypeBuilderDefinition<T> columnDefinition;
        private final int index;
        public RetrievedByIndexColumn(ColumnStorableTypeBuilderDefinition<T> columnDefinition, int index) {
            this.columnDefinition = columnDefinition;
            this.index = index;
        }

        @Override
        public String getColumnName() {
            return columnDefinition.getColumnName();
        }

        @Override
        public PreparedStatementParameterSetter<T> createStatementSetter(PreparedStatement statement) {
            return columnDefinition.createStatementSetter(statement);
        }

        @Override
        public T retrieveValue(ResultSet resultSet) throws SQLException {
            return columnDefinition.retrieveValue(resultSet, index);
        }

        @Override
        public T retrieveValue(ResultSet resultSet, String label) throws SQLException {
            return columnDefinition.retrieveValue(resultSet, label);
        }

        @Override
        public T retrieveValue(ResultSet resultSet, int index) throws SQLException {
            return columnDefinition.retrieveValue(resultSet, index);
        }
    }

    private static class BooleanColumn implements ColumnStorableTypeBuilderDefinition<Boolean> {
        private final String columnName;

        public BooleanColumn(String columnName) {
            this.columnName = columnName;
        }

        @Override
        public String getColumnName() {
            return columnName;
        }

        @Override
        public PreparedStatementParameterSetter<Boolean> createStatementSetter(final PreparedStatement statement) {
            return new PreparedStatementParameterSetter<Boolean>() {
                @Override
                public void setValue(int index, Boolean value) throws SQLException {
                    statement.setBoolean(index, value);
                }
            };
        }

        @Override
        public Boolean retrieveValue(ResultSet resultSet) throws SQLException {
            return retrieveValue(resultSet, columnName);
        }

        @Override
        public Boolean retrieveValue(ResultSet resultSet, String label) throws SQLException {
            return resultSet.getBoolean(label);
        }

        @Override
        public Boolean retrieveValue(ResultSet resultSet, int index) throws SQLException {
            return resultSet.getBoolean(index);
        }
    }
}
