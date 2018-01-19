package org.usfirst.frc.team2225.season2018.roboRIO.commands;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.command.Command;
import org.usfirst.frc.team2225.season2018.roboRIO.RoboRIOMain;

public class ArcadeDrive extends Command {

    public ArcadeDrive() {
        requires(RoboRIOMain.drivetrain);
        requires(RoboRIOMain.driverInput);
    }

    @Override
    protected void execute() {
        RoboRIOMain.drivetrain.arcadeDrive(RoboRIOMain.driverInput.getJoy().getY(GenericHID.Hand.kLeft), RoboRIOMain.driverInput.getJoy().getX(GenericHID.Hand.kLeft));
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
