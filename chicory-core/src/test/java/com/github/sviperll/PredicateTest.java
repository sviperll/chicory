/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class PredicateTest {

    public PredicateTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of truePredicate method, of class Predicate.
     */
    @Test
    public void testTruePredicate() {
        assertTrue(Predicate.truePredicate().evaluate(null));
    }

    /**
     * Test of falsePredicate method, of class Predicate.
     */
    @Test
    public void testFalsePredicate() {
        assertFalse(Predicate.falsePredicate().evaluate(null));
    }

    /**
     * Test of not method, of class Predicate.
     */
    @Test
    public void testNot() {
        assertTrue(Predicate.truePredicate().evaluate(null));
        assertFalse(Predicate.truePredicate().not().evaluate(null));
        assertTrue(Predicate.truePredicate().not().not().evaluate(null));
        assertFalse(Predicate.truePredicate().not().not().not().evaluate(null));
        assertTrue(Predicate.truePredicate().not().not().not().not().evaluate(null));
    }

    /**
     * Test of and method, of class Predicate.
     */
    @Test
    public void testAnd_Evaluatable() {
        Predicate<Object> f = Predicate.falsePredicate();
        Predicate<Object> t = Predicate.truePredicate();

        assertEvaluatesToFalse(f.and(f));
        assertEvaluatesToFalse(f.and(t));
        assertEvaluatesToFalse(t.and(f));
        assertEvaluatesToTrue(t.and(t));

        assertEvaluatesToTrue(t.and(t).and(t).and(t).and(t));
        assertEvaluatesToFalse(f.and(t).and(t).and(t).and(t));
        assertEvaluatesToFalse(t.and(f).and(t).and(t).and(t));
        assertEvaluatesToFalse(t.and(t).and(f).and(t).and(t));
        assertEvaluatesToFalse(t.and(t).and(t).and(f).and(t));
        assertEvaluatesToFalse(t.and(t).and(t).and(t).and(f));
    }

    /**
     * Test of or method, of class Predicate.
     */
    @Test
    public void testOr_Evaluatable() {
        Predicate<Object> f = Predicate.falsePredicate();
        Predicate<Object> t = Predicate.truePredicate();

        assertEvaluatesToFalse(f.or(f));
        assertEvaluatesToTrue(f.or(t));
        assertEvaluatesToTrue(t.or(f));
        assertEvaluatesToTrue(t.or(t));

        assertEvaluatesToFalse(f.or(f).or(f).or(f).or(f));
        assertEvaluatesToTrue(t.or(f).or(f).or(f).or(f));
        assertEvaluatesToTrue(f.or(t).or(f).or(f).or(f));
        assertEvaluatesToTrue(f.or(f).or(t).or(f).or(f));
        assertEvaluatesToTrue(f.or(f).or(f).or(t).or(f));
        assertEvaluatesToTrue(f.or(f).or(f).or(f).or(t));
    }

    private void assertEvaluatesToTrue(Predicate<?> p) {
        assertTrue(p.evaluate(null));
    }

    private void assertEvaluatesToFalse(Predicate<?> p) {
        assertFalse(p.evaluate(null));
    }
}
