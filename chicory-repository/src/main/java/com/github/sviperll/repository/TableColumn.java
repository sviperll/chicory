/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.repository;

import com.github.sviperll.IsomorphismDefinition;
import com.github.sviperll.repository.TableColumn.PreparedStatementParameterSetter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class ConfigurableTableColumn<T> implements TableColumn<T> {
    private final TableColumn<T> column;
    public ConfigurableTableColumn(TableColumn<T> column) {
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

    public <U> ConfigurableTableColumn<U> isomorphic(IsomorphismDefinition<U, T> structure) {
        return TableColumns.isomorphic(column, structure);
    }

    public ConfigurableTableColumn<T> retrievedByIndex(int index) {
        return TableColumns.retrievedByIndex(column, index);
    }
}
