package org.usfirst.frc.team2225.season2018.roboRIO.commands;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.command.Command;
import org.usfirst.frc.team2225.season2018.roboRIO.RoboRIOMain;
import org.usfirst.frc.team2225.season2018.roboRIO.Vector2D;
import org.usfirst.frc.team2225.season2018.roboRIO.subsystems.Drivetrain;
import org.usfirst.frc.team2225.season2018.roboRIO.subsystems.Lifter;

public class Teleop extends Command {

    public Teleop() {
        requires(RoboRIOMain.drivetrain);
        requires(RoboRIOMain.lifter);
        //requires(RoboRIOMain.sucker);
    }

    double scaleInputs(double valIn) {
        double[] param = {0.2, 0.3, 2};
        double val = valIn;
        val = Drivetrain.deadzone(param[0], val);
        val = Math.copySign(Math.pow(Math.abs(val), param[2]), val);

        if(Math.abs(val) > 0)
            val = Drivetrain.padMinValue(param[1], val, true);
        return val;
    }

    @Override
    protected void execute() {
        XboxController joy = RoboRIOMain.driverInput.getJoy();
        Vector2D translate = new Vector2D();
        translate.x = scaleInputs(joy.getX(GenericHID.Hand.kLeft));
        translate.y = scaleInputs(-joy.getY(GenericHID.Hand.kLeft));
        double rotate = scaleInputs(-joy.getX(GenericHID.Hand.kRight));
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
