package org.usfirst.frc.team2225.season2018.roboRIO.subsystems;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.command.Subsystem;

public class DriverInput extends Subsystem {
    private Joystick joy;

    public DriverInput() {
        joy = new Joystick(0);
    }

    public Joystick getJoy() {
        return joy;
    }

    @Override
    protected void initDefaultCommand() {

    }
}
