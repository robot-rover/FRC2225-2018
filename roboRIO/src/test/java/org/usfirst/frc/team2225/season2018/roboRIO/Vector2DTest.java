package org.usfirst.frc.team2225.season2018.roboRIO;

import org.junit.Test;

import static java.lang.Math.PI;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class Vector2DTest {
    static Vector2D zeros;
    static Vector2D unitX;
    static Vector2D unitY;
    static Vector2D unitNX;
    static Vector2D unitNY;
    static Vector2D firstQuadrant;
    static Vector2D secondQuadrant;
    static Vector2D thirdQuadrant;
    static Vector2D fourthQuadrant;
    static Vector2D test1;
    static Vector2D test2;
    static Vector2D test3;
    static Vector2D test4;
    final double maxDelta = 0.00000001;

    private void resetFields() {
        zeros = new Vector2D();
        unitX = new Vector2D(1, 0);
        unitY = new Vector2D(0, 1);
        unitNX = new Vector2D(-1, 0);
        unitNY = new Vector2D(0, -1);
        firstQuadrant = new Vector2D(1, 1);
        secondQuadrant = new Vector2D(-1, 1);
        thirdQuadrant = new Vector2D(-1, -1);
        fourthQuadrant = new Vector2D(1, -1);
        test1 = new Vector2D(10, 5.8);
        test2 = new Vector2D(-7, 8);
        test3 = new Vector2D(-4, -4);
        test4 = new Vector2D(1.5, -5);
    }

    @Test
    public void ofDirectionTest() {
        resetFields();
        assertArrayEquals(zeros.export(), Vector2D.ofDirection(0, 100).export(), maxDelta);
        assertArrayEquals(unitX.export(), Vector2D.ofDirection(1, 0).export(), maxDelta);
        assertArrayEquals(unitY.export(), Vector2D.ofDirection(1, PI / 2).export(), maxDelta);
        assertArrayEquals(unitNX.export(), Vector2D.ofDirection(1, PI).export(), maxDelta);
        assertArrayEquals(unitNY.export(), Vector2D.ofDirection(1, PI * 3 / 2).export(), maxDelta);
        assertArrayEquals(unitY.export(), Vector2D.ofDirection(-1, PI * 3 / 2).export(), maxDelta);
        assertArrayEquals(firstQuadrant.export(), Vector2D.ofDirection(Math.sqrt(2), PI / 4).export(), maxDelta);
        assertArrayEquals(secondQuadrant.export(), Vector2D.ofDirection(Math.sqrt(2), PI * 3 / 4).export(), maxDelta);
        assertArrayEquals(secondQuadrant.export(), Vector2D.ofDirection(Math.sqrt(2), PI * 3 / 4).export(), maxDelta);
        assertArrayEquals(secondQuadrant.export(), Vector2D.ofDirection(-Math.sqrt(2), PI * 7 / 4).export(), maxDelta);
    }

    @Test
    public void magnitudeTest() {
        resetFields();
        assertEquals(0, zeros.magnitude(), maxDelta);
        assertEquals(1, unitX.magnitude(), maxDelta);
        assertEquals(1, unitY.magnitude(), maxDelta);
        assertEquals(1, unitNX.magnitude(), maxDelta);
        assertEquals(1, unitNY.magnitude(), maxDelta);
        assertEquals(Math.sqrt(2), firstQuadrant.magnitude(), maxDelta);
        assertEquals(Math.sqrt(2), secondQuadrant.magnitude(), maxDelta);
        assertEquals(Math.sqrt(2), thirdQuadrant.magnitude(), maxDelta);
        assertEquals(Math.sqrt(2), fourthQuadrant.magnitude(), maxDelta);
        assertEquals(11.56027681, test1.magnitude(), maxDelta);
        assertEquals(10.63014581, test2.magnitude(), maxDelta);
        assertEquals(5.656854249, test3.magnitude(), maxDelta);
        assertEquals(5.220153254, test4.magnitude(), maxDelta);
    }

    @Test
    public void dotTest() {
        resetFields();
        assertEquals(10, unitX.dot(test1), maxDelta);
        assertEquals(8, unitY.dot(test2), maxDelta);
        assertEquals(-5.8, test1.dot(unitNY), maxDelta);
        assertEquals(-10, test1.dot(unitNX), maxDelta);
        assertEquals(14, test3.dot(test4), maxDelta);
        assertEquals(0, firstQuadrant.dot(secondQuadrant), maxDelta);
        assertEquals(8, thirdQuadrant.dot(test3), maxDelta);
        assertEquals(0, test4.dot(zeros), maxDelta);
    }

    @Test
    public void getDirectionTest() {
        resetFields();
        assertEquals(0, zeros.getDirection(), maxDelta);
        assertEquals(0, unitX.getDirection(), maxDelta);
        assertEquals(PI / 2, unitY.getDirection(), maxDelta);
        assertEquals(PI, unitNX.getDirection(), maxDelta);
        assertEquals(PI * 3 / 2, unitNY.getDirection(), maxDelta);
        assertEquals(PI / 4, firstQuadrant.getDirection(), maxDelta);
        assertEquals(PI * 3 / 4, secondQuadrant.getDirection(), maxDelta);
        assertEquals(PI * 5 / 4, thirdQuadrant.getDirection(), maxDelta);
        assertEquals(PI * 7 / 4, fourthQuadrant.getDirection(), maxDelta);
        assertEquals(0.5255837936, test1.getDirection(), maxDelta);
        assertEquals(2.289626326, test2.getDirection(), maxDelta);
        assertEquals(PI * 5 / 4, test3.getDirection(), maxDelta);
        assertEquals(5.003845775, test4.getDirection(), maxDelta);
    }
}
