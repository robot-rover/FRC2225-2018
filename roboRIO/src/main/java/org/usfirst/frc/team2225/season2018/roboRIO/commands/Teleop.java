package org.usfirst.frc.team2225.season2018.roboRIO.commands;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.command.Command;
import org.usfirst.frc.team2225.season2018.roboRIO.RoboRIOMain;
import org.usfirst.frc.team2225.season2018.roboRIO.Vector2D;
import org.usfirst.frc.team2225.season2018.roboRIO.subsystems.Lifter;

public class Teleop extends Command {

    public Teleop() {
        requires(RoboRIOMain.drivetrain);
        requires(RoboRIOMain.lifter);
        //requires(RoboRIOMain.sucker);
    }

    @Override
    protected void execute() {
        XboxController joy = RoboRIOMain.driverInput.getJoy();
        Vector2D translate = new Vector2D();
        translate.x = signSquare(joy.getX(GenericHID.Hand.kLeft));
        translate.y = signSquare(-joy.getY(GenericHID.Hand.kLeft));
        double rotate = -joy.getX(GenericHID.Hand.kRight);
        RoboRIOMain.drivetrain.omniDrive(translate, Math.copySign(rotate * rotate, rotate));
        double suckerOut = 0;
        double suckerLim = 0.8;
        //should be suck in (input is positive)
        if(joy.getAButton()) suckerOut += suckerLim;
        if(joy.getBButton()) suckerOut -= suckerLim;
        RoboRIOMain.sucker.suck(suckerOut);
        double turnOut = 0.0;
        if(joy.getYButton())
            turnOut += suckerLim;
        if(joy.getXButton())
            turnOut -= suckerLim;
        if(turnOut != 0.0)
            RoboRIOMain.sucker.turn(turnOut);
        RoboRIOMain.lifter.move(-joy.getTriggerAxis(GenericHID.Hand.kLeft) + joy.getTriggerAxis(GenericHID.Hand.kRight) * 0.7);

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
