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

import com.github.sviperll.OptionalVisitor;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MySQLRepositorySupportDefinition implements RepositorySupportDefinition {
    private static <T, U> boolean isElementsEquals(AtomicStorableComponent<T, U> definition, T value1, T value2) {
        U elementValue1 = definition.getComponent(value1);
        U elementValue2 = definition.getComponent(value2);
        if (elementValue1 == elementValue2)
            return true;
        else if (elementValue1 == null || elementValue2 == null)
            return false;
        else
            return elementValue1.equals(elementValue2);
    }

    private final SQLConnection connection;

    public MySQLRepositorySupportDefinition(SQLConnection connection) {
        this.connection = connection;
    }

    @Override
    public <K, V> K putNewEntry(AutogeneratedKeyIndexedRepositoryConfiguration<K, V> configuration, V attributes) throws SQLException {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.append("INSERT INTO ").append(configuration.getTableName()).append(" (");
        List<? extends AtomicStorableComponent<V, ?>> elements = configuration.getValueDefinition().getAtomicComponents();
        sqlBuilder.appendJoinedTupleElements(", ", "{0}", elements);
        sqlBuilder.append(") VALUES (");
        sqlBuilder.appendJoinedTupleElements(", ", "?", elements);
        sqlBuilder.append(")");
        String sql = sqlBuilder.toString();
        try (PreparedStatement statement = connection.prepareStatementWithAutogeneratedKeys(sql)) {
            StatementSetter statementSetter = new StatementSetter(statement);
            for (AtomicStorableComponent<V, ?> element: elements)
                statementSetter.setElement(element, attributes);
            int insertedRowsCount = statement.executeUpdate();
            if (insertedRowsCount != 1)
                throw new IllegalStateException("Unable to insert into " + configuration.getTableName());
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                boolean hasNextRecord = resultSet.next();
                if (!hasNextRecord)
                    throw new IllegalStateException("No autogenerated key for " + configuration.getTableName());
                return configuration.getAutogeneratedKeyDefinition().createInstance(resultSet);
            }
        }
    }

    @Override
    public <K, V, R, E extends Exception> R get(IndexedRepositoryConfiguration<K, V> configuration, K key, OptionalVisitor<V, R, E> optionalVisitor)
            throws E, SQLException {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.append("SELECT * FROM ").append(configuration.getTableName());
        List<? extends AtomicStorableComponent<K, ?>> keyElements = configuration.getKeyDefinition().getAtomicComponents();
        if (!keyElements.isEmpty()) {
            sqlBuilder.append(" WHERE ");
            sqlBuilder.appendJoinedTupleElements(" AND ", "{0} = ?", keyElements);
        }
        String sql = sqlBuilder.toString();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            StatementSetter statementSetter = new StatementSetter(statement);
            for (AtomicStorableComponent<K, ?> element: keyElements)
                statementSetter.setElement(element, key);
            try (ResultSet resultSet = statement.executeQuery()) {
                boolean hasNextRecord = resultSet.next();
                if (!hasNextRecord)
                    return optionalVisitor.missing();
                else {
                    V value = configuration.getValueDefinition().createInstance(resultSet);
                    return optionalVisitor.present(value);
                }
            }
        }
    }

    @Override
    public <K, V, O> List<V> entryList(ReadableRepositoryDirectoryConfiguration<K, V, O> configuration, K key, SlicingQuery<O> slicing) throws SQLException {
        List<? extends AtomicStorableComponent<O, ?>> orderingElements = configuration.getOrderingDefinition().getAtomicComponents();
        List<? extends AtomicStorableComponent<K, ?>> keyElements = configuration.getKeyDefinition().getAtomicComponents();
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.append("SELECT * FROM ");
        sqlBuilder.append(configuration.getTableName());
        if (slicing.hasConditions() || !keyElements.isEmpty())
            sqlBuilder.append(" WHERE ");
        if (!keyElements.isEmpty()) {
            sqlBuilder.appendJoinedTupleElements(" AND ", "{0} = ?", keyElements);
            if (slicing.hasConditions())
                sqlBuilder.append(" AND ");
        }
        if (slicing.hasConditions()) {
            if (orderingElements.isEmpty())
                throw new IllegalArgumentException("Ordering definition shouldn't be empty!");
            else {
                if (orderingElements.size() == 1)
                    sqlBuilder.appendConditionForColumn(slicing.condition(), orderingElements.get(0));
                else {
                    sqlBuilder.append("(");
                    sqlBuilder.appendConditionForColumn(slicing.condition(), orderingElements.get(0));
                    for (int i = 1; i < orderingElements.size(); i++) {
                        sqlBuilder.append("OR (");
                        sqlBuilder.appendJoinedTupleElements(" AND ", "{0} = ?", orderingElements.subList(0, i));
                        sqlBuilder.append(" AND ");
                        sqlBuilder.appendConditionForColumn(slicing.condition(), orderingElements.get(i));
                        sqlBuilder.append(")");
                    }
                    sqlBuilder.append(")");
                }
            }
        }
        if (slicing.isOrdered()) {
            sqlBuilder.append(" ORDER BY ");
            if (orderingElements.isEmpty())
                throw new IllegalArgumentException("Ordering definition shouldn't be empty!");
            else {
                String format = slicing.isDescending() ? "{0} DESC" : "{0} ASC";
                sqlBuilder.appendJoinedTupleElements(", ", format, orderingElements);
            }
        }
        if (slicing.hasLimit())
            sqlBuilder.append(" LIMIT ?");
        String sql = sqlBuilder.toString();
        try {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                StatementSetter statementSetter = new StatementSetter(statement);
                for (AtomicStorableComponent<K, ?> element: keyElements)
                    statementSetter.setElement(element, key);
                if (slicing.hasConditions()) {
                    for (int i = 0; i < orderingElements.size(); i++) {
                        for (int j = 0; j < i; j++)
                            statementSetter.setElement(orderingElements.get(j), slicing.condition().value());
                        statementSetter.setElement(orderingElements.get(i), slicing.condition().value());
                    }
                }
                if (slicing.hasLimit())
                    statementSetter.setInt(slicing.limit());
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<V> result;
                    if (slicing.hasLimit())
                        result = new ArrayList<>(slicing.limit());
                    else
                        result = new ArrayList<>();
                    while (resultSet.next()) {
                        V entry = configuration.getEntryDefinition().createInstance(resultSet);
                        result.add(entry);
                    }
                    if (slicing.postProcessing().needsToBeReveresed())
                        Collections.reverse(result);
                    return result;
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Error executing query: " + sql, ex);
        }
    }

    @Override
    public <K> boolean remove(RepositoryIndex<K> configuration, K key) throws SQLException {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.append("DELETE FROM ").append(configuration.getTableName());
        List<? extends AtomicStorableComponent<K, ?>> keyElements = configuration.getKeyDefinition().getAtomicComponents();
        if (!keyElements.isEmpty()) {
            sqlBuilder.append(" WHERE ");
            sqlBuilder.appendJoinedTupleElements(" AND ", "{0} = ?", keyElements);
        }
        String sql = sqlBuilder.toString();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            StatementSetter statementSetter = new StatementSetter(statement);
            for (AtomicStorableComponent<K, ?> element: configuration.getKeyDefinition().getAtomicComponents())
                statementSetter.setElement(element, key);
            int removedRecordsCount = statement.executeUpdate();
            if (removedRecordsCount > 1)
                throw new IllegalStateException(
                        "Removed several records from " + configuration.getTableName() + " table with single key value: " + key);
            return removedRecordsCount == 1;
        }
    }

    @Override
    public <K, V> boolean putIfExists(IndexedRepositoryConfiguration<K, V> configuration, K key, Changes<V> attributes) throws SQLException {
        ArrayList<String> assignments = new ArrayList<>();
        List<? extends AtomicStorableComponent<V, ?>> valueElements = configuration.getValueDefinition().getAtomicComponents();
        for (AtomicStorableComponent<V, ?> element: valueElements) {
            if (!isElementsEquals(element, attributes.oldValue(), attributes.newValue())) {
                Object oldElementValueObject = element.getComponent(attributes.oldValue());
                if (oldElementValueObject instanceof Integer) {
                    int oldInt = (Integer)oldElementValueObject;
                    int newInt = (Integer)element.getComponent(attributes.newValue());
                    int delta = newInt - oldInt;
                    String name = element.getColumn().getColumnName();
                    if (delta >= 0)
                        assignments.add(name + " = " + name + " + " + delta);
                    else
                        assignments.add(name + " = " + name + " - " + (-delta));
                } else if (oldElementValueObject instanceof Long) {
                    long oldInt = (Long)oldElementValueObject;
                    long newInt = (Long)element.getComponent(attributes.newValue());
                    long delta = newInt - oldInt;
                    String name = element.getColumn().getColumnName();
                    if (delta >= 0)
                        assignments.add(name + " = " + name + " + " + delta);
                    else
                        assignments.add(name + " = " + name + " - " + (-delta));
                } else
                    assignments.add(element.getColumn().getColumnName() + " = ?");
            }
        }
        if (assignments.isEmpty()) {
            return false;
        } else {
            SQLBuilder sqlBuilder = new SQLBuilder();
            sqlBuilder.append("UPDATE ").append(configuration.getTableName()).append(" SET ");
            sqlBuilder.appendJoined(", ", assignments);

            List<? extends AtomicStorableComponent<K, ?>> keyElements = configuration.getKeyDefinition().getAtomicComponents();
            if (!keyElements.isEmpty()) {
                sqlBuilder.append(" WHERE ");
                sqlBuilder.appendJoinedTupleElements(" AND ", "{0} = ?", keyElements);
            }
            String sql = sqlBuilder.toString();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                StatementSetter statementSetter = new StatementSetter(statement);
                for (AtomicStorableComponent<V, ?> element: valueElements) {
                    if (!isElementsEquals(element, attributes.oldValue(), attributes.newValue())) {
                        Object oldElementValueObject = element.getComponent(attributes.oldValue());
                        if (!(oldElementValueObject instanceof Integer)
                            && !(oldElementValueObject instanceof Long)) {
                            statementSetter.setElement(element, attributes.newValue());
                        }
                    }
                }
                for (AtomicStorableComponent<K, ?> element: keyElements) {
                    statementSetter.setElement(element, key);
                }

                int removedRecordsCount = statement.executeUpdate();
                if (removedRecordsCount > 1)
                    throw new IllegalStateException(
                            "Updated several " + configuration.getTableName() + " with single id value: " + key);
                return removedRecordsCount == 1;
            }
        }
    }

    @Override
    public <K, V> void putNewEntry(IndexedRepositoryConfiguration<K, V> configuration, K key, V attributes) throws SQLException {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.append("INSERT INTO ").append(configuration.getTableName()).append(" (");
        List<? extends AtomicStorableComponent<K, ?>> keyElements = configuration.getKeyDefinition().getAtomicComponents();
        List<? extends AtomicStorableComponent<V, ?>> valueElements = configuration.getValueDefinition().getAtomicComponents();
        sqlBuilder.appendJoinedTupleElements(", ", "{0}", keyElements);
        sqlBuilder.appendJoinedTupleElements(", ", "{0}", valueElements);
        sqlBuilder.append(") VALUES (");
        sqlBuilder.appendJoinedTupleElements(", ", "?", keyElements);
        sqlBuilder.appendJoinedTupleElements(", ", "?", valueElements);
        sqlBuilder.append(")");
        String sql = sqlBuilder.toString();
        try (PreparedStatement statement = connection.prepareStatementWithoutAutogeneratedKeys(sql)) {
            StatementSetter statementSetter = new StatementSetter(statement);
            for (AtomicStorableComponent<K, ?> element: keyElements)
                statementSetter.setElement(element, key);
            for (AtomicStorableComponent<V, ?> element: valueElements)
                statementSetter.setElement(element, attributes);
            int insertedRowsCount = statement.executeUpdate();
            if (insertedRowsCount != 1)
                throw new IllegalStateException("Unable to insert into " + configuration.getTableName());
        }
    }

    @Override
    public SQLConnection connection() {
        return connection;
    }

    private static class StatementSetter {
        private final PreparedStatement statement;
        private int index;
        public StatementSetter(PreparedStatement statement, int index) {
            this.statement = statement;
            this.index = index;
        }

        private StatementSetter(PreparedStatement statement) {
            this(statement, 1);
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public <T, U> void setElement(AtomicStorableComponent<T, U> definition, T value) throws SQLException {
            U columnValue = definition.getComponent(value);
            definition.getColumn().createStatementSetter(statement).setValue(index, columnValue);
            index++;
        }

        private void setInt(int value) throws SQLException {
            statement.setInt(index, value);
            index++;
        }
    }
}
