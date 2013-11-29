/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.repository;

import java.sql.Connection;

/**
 *
 * @author vir
 */
public class SQLConnections {
    public static SQLConnection createInstance(Connection connection) {
        return SimpleSQLConnection.createInstance(connection);
    }
}
