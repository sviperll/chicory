package com.github.sviperll.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @param <U> column value data-type
 */
public interface TableColumn<U> {
    String getColumnName();

    PreparedStatementParameterSetter<U> createStatementSetter(PreparedStatement statement);
    U retrieveValue(ResultSet resultSet) throws SQLException;
    U retrieveValue(ResultSet resultSet, String label) throws SQLException;
    U retrieveValue(ResultSet resultSet, int index) throws SQLException;

    public interface PreparedStatementParameterSetter<U> {
        void setValue(int index, U value) throws SQLException;
    }
}
