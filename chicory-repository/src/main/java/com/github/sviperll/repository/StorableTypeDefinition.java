package com.github.sviperll.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface StorableTypeDefinition<T> {
    List<? extends AtomicStorableComponent<T, ?>> getAtomicComponents();

    T createInstance(ResultSet resultSet) throws SQLException;
}
