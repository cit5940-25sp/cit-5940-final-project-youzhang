package models;

import org.junit.Test;
import static org.junit.Assert.*;

public class TupleTest {
    @Test
    public void testTupleCreation() {
        Tuple<String, Integer> tuple = new Tuple<>("test", 1);
        assertEquals("test", tuple.getLeft());
        assertEquals(Integer.valueOf(1), tuple.getRight());
    }

    @Test
    public void testSetLeft() {
        Tuple<String, Integer> tuple = new Tuple<>("test", 1);
        tuple.setLeft("new");
        assertEquals("new", tuple.getLeft());
    }

    @Test
    public void testSetRight() {
        Tuple<String, Integer> tuple = new Tuple<>("test", 1);
        tuple.setRight(2);
        assertEquals(Integer.valueOf(2), tuple.getRight());
    }

    @Test
    public void testNullValues() {
        Tuple<String, Integer> tuple = new Tuple<>(null, null);
        assertNull(tuple.getLeft());
        assertNull(tuple.getRight());
        
        tuple.setLeft("test");
        tuple.setRight(1);
        assertEquals("test", tuple.getLeft());
        assertEquals(Integer.valueOf(1), tuple.getRight());
    }

    @Test
    public void testDifferentTypes() {
        Tuple<Integer, String> tuple = new Tuple<>(1, "test");
        assertEquals(Integer.valueOf(1), tuple.getLeft());
        assertEquals("test", tuple.getRight());
        
        tuple.setLeft(2);
        tuple.setRight("new");
        assertEquals(Integer.valueOf(2), tuple.getLeft());
        assertEquals("new", tuple.getRight());
    }

    @Test
    public void testBooleanType() {
        Tuple<Boolean, Boolean> tuple = new Tuple<>(true, false);
        assertTrue(tuple.getLeft());
        assertFalse(tuple.getRight());
        
        tuple.setLeft(false);
        tuple.setRight(true);
        assertFalse(tuple.getLeft());
        assertTrue(tuple.getRight());
    }
} 