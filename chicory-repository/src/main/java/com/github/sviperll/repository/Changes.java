/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.repository;

/**
 *
 * @author vir
 */
public class Changes<T> {
    public static <T> Changes<T> fromTo(T oldValue, T newValue) {
        return new Changes<>(oldValue, newValue);
    }

    private final T oldValue;
    private final T newValue;
    public Changes(T oldValue, T newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public T oldValue() {
        return oldValue;
    }

    public T newValue() {
        return newValue;
    }
}
