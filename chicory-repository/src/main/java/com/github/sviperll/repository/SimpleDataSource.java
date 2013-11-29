/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.repository;

import com.github.sviperll.Credentials;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class SimpleDataSource implements DataSource {
    public static SimpleDataSource createInstance(String jdbcURL) {
        return new SimpleDataSource(jdbcURL, null);
    }

    public static SimpleDataSource createInstance(String jdbcURL, String user, String password) {
        return new SimpleDataSource(jdbcURL, new Credentials(user, password));
    }

    private final String jdbcURL;
    private final Credentials credentials;

    public SimpleDataSource(String jdbcURL, Credentials credentials) {
        this.jdbcURL = jdbcURL;
        this.credentials = credentials;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (credentials == null)
            return DriverManager.getConnection(jdbcURL);
        else
            return DriverManager.getConnection(jdbcURL, credentials.userName(), credentials.password());
    }

    @Override
    public Connection getConnection(String user, String password) throws SQLException {
        return DriverManager.getConnection(jdbcURL, user, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
}
