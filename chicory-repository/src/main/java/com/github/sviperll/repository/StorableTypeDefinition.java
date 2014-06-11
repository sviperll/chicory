package com.github.sviperll.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface StorableClass<T> {
    List<? extends AtomicStorableClassComponent<T, ?>> getAtomicComponents();

    T createInstance(ResultSet resultSet) throws SQLException;
}
