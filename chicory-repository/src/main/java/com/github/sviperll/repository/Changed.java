/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.repository;

/**
 *
 * @author vir
 */
public class Changed<T> {
    public static <T> Changed<T> fromTo(T oldValue, T newValue) {
        return new Changed<T>(oldValue, newValue);
    }

    private final T oldValue;
    private final T newValue;
    public Changed(T oldValue, T newValue) {
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
