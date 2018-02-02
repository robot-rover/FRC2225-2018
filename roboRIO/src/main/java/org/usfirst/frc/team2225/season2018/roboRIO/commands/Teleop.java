package org.usfirst.frc.team2225.season2018.roboRIO.commands;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.command.Command;
import org.usfirst.frc.team2225.season2018.roboRIO.RoboRIOMain;
import org.usfirst.frc.team2225.season2018.roboRIO.Vector2D;

public class Teleop extends Command {

    public Teleop() {
        requires(RoboRIOMain.drivetrain);
    }

    @Override
    protected void execute() {
        Vector2D translate = new Vector2D();
        translate.x = signSquare(RoboRIOMain.driverInput.getJoy().getX(GenericHID.Hand.kLeft));
        translate.y = signSquare(-RoboRIOMain.driverInput.getJoy().getY(GenericHID.Hand.kLeft));
        double rotate = RoboRIOMain.driverInput.getJoy().getTriggerAxis(GenericHID.Hand.kRight) - RoboRIOMain.driverInput.getJoy().getTriggerAxis(GenericHID.Hand.kLeft);
        RoboRIOMain.drivetrain.omniDrive(translate, Math.copySign(rotate * rotate, rotate));

    }

    private double signSquare(double val) {
        return Math.signum(val) * val * val;
    }

    @Override
    protected boolean isFinished() {
        return false;

    }

    @Override
    protected void end() {
        RoboRIOMain.drivetrain.reset();
    }
}
