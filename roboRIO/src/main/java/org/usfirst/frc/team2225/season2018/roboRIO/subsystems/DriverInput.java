package org.usfirst.frc.team2225.season2018.roboRIO.subsystems;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.command.Subsystem;

public class DriverInput extends Subsystem {
    private XboxController joy;

    public DriverInput() {
        joy = new XboxController(0);
    }

    public XboxController getJoy() {
        return joy;
    }

    @Override
    protected void initDefaultCommand() {

    }
}
