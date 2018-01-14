package org.usfirst.frc.team2225.season2018.roboRIO.subsystems;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.usfirst.frc.team2225.season2018.roboRIO.subsystems.Drivetrain.padValue;

public class DrivetrainTest {
    final double maxDelta = 0.00000001;

    @Test
    public void padValueTest() {
        assertEquals(0.5, padValue(0.5, 1, false), maxDelta);
        assertEquals(1, padValue(0.5, 1, true), maxDelta);
        assertEquals(0.75, padValue(0.5, 0.5, true), maxDelta);
        assertEquals(-0.25, padValue(0.5, -0.5, false), maxDelta);
        assertEquals(-0.9, padValue(0.5, -0.8, true), maxDelta);
    }
}
