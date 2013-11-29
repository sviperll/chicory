/*
 * Copyright (C) 2013 Victor Nazarov <asviraspossible@gmail.com>
 */

package com.github.sviperll.repository;

import com.github.sviperll.time.Times;
import com.github.sviperll.time.UnixTime;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.github.sviperll.repository.TableColumn.PreparedStatementParameterSetter;
import com.github.sviperll.IsomorphismDefinition;

public class TableColumns {
    public static <T> ConfigurableTableColumn<T> configure(TableColumn<T> column) {
        if (column instanceof ConfigurableTableColumn)
            return (ConfigurableTableColumn<T>)column;
        else
            return new ConfigurableTableColumn<T>(column);
    }

    public static ConfigurableTableColumn<Integer> integer(String columnName) {
        return new ConfigurableTableColumn<Integer>(new IntegerColumn(columnName));
    }

    public static ConfigurableTableColumn<String> string(String columnName) {
        return new ConfigurableTableColumn<String>(new StringColumn(columnName));
    }

    public static ConfigurableTableColumn<UnixTime> unixTime(String columnName) {
        return new ConfigurableTableColumn<UnixTime>(new UnixTimeColumn(columnName));
    }

    public static <T, U> ConfigurableTableColumn<T> isomorphic(TableColumn<U> columnDefinition, IsomorphismDefinition<T, U> structure) {
        return new ConfigurableTableColumn<T>(new IsomorphicColumn<T, U>(columnDefinition, structure));
    }

    public static <T> ConfigurableTableColumn<T> retrievedByIndex(TableColumn<T> columnDefinition, int index) {
        return new ConfigurableTableColumn<T>(new RetrievedByIndexColumn<T>(columnDefinition, index));
    }

    public static ConfigurableTableColumn<Boolean> booleanColumn(final String columnName) {
        return new ConfigurableTableColumn<Boolean>(new BooleanColumn(columnName));
    }

    private TableColumns() {
    }

    private static class IntegerColumn implements TableColumn<Integer> {
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

    private static class StringColumn implements TableColumn<String> {
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

    private static class UnixTimeColumn implements TableColumn<UnixTime> {
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
            return Times.resultSet(resultSet).getUnixTime(label, Times.GMT_OFFSET);
        }

        @Override
        public UnixTime retrieveValue(ResultSet resultSet, int index) throws SQLException {
            return Times.resultSet(resultSet).getUnixTime(index, Times.GMT_OFFSET);
        }

        private static class UnixTimeColumnStatementSetter implements PreparedStatementParameterSetter<UnixTime> {
            private final PreparedStatement statement;

            public UnixTimeColumnStatementSetter(PreparedStatement statement) {
                this.statement = statement;
            }

            @Override
            public void setValue(int index, UnixTime value) throws SQLException {
                Times.preparedStatement(statement).setUnixTime(index, value, Times.GMT_OFFSET);
            }
        }
    }

    private static class IsomorphicColumn<T, U> implements TableColumn<T> {
        private final TableColumn<U> columnDefinition;
        private final IsomorphismDefinition<T, U> structure;
        public IsomorphicColumn(TableColumn<U> columnDefinition, IsomorphismDefinition<T, U> structure) {
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

    private static class RetrievedByIndexColumn<T> implements TableColumn<T> {
        private final TableColumn<T> columnDefinition;
        private final int index;
        public RetrievedByIndexColumn(TableColumn<T> columnDefinition, int index) {
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

    private static class BooleanColumn implements TableColumn<Boolean> {
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
