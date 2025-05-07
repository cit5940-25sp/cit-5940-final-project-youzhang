package test.models;

import models.Tuple;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TupleTest {
    private Tuple<String, Integer> tuple;

    @Before
    public void setUp() {
        tuple = new Tuple<>("test", 1);
    }

    @Test
    public void testGetLeft() {
        assertEquals("test", tuple.getLeft());
    }

    @Test
    public void testGetRight() {
        assertEquals(Integer.valueOf(1), tuple.getRight());
    }

    @Test
    public void testSetLeft() {
        tuple.setLeft("new");
        assertEquals("new", tuple.getLeft());
    }

    @Test
    public void testSetRight() {
        tuple.setRight(2);
        assertEquals(Integer.valueOf(2), tuple.getRight());
    }
} 