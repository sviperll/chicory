/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.repository;

import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author vir
 */
public interface ReadableRepository<E, O> {

    List<E> entryList(SlicingQuery<O> slicing) throws SQLException;

}
