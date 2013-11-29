/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.repository;

import java.sql.SQLException;

/**
 *
 * @author vir
 */
public interface SQLTransactionManager {
    void beginTransaction() throws SQLException;

    void beginTransaction(SQLTransactionIsolationLevel level) throws SQLException;

    void commitTransaction() throws SQLException;

    void rollbackTransaction() throws SQLException;

    void rollbackTransactionIfNotCommited() throws SQLException;

}
