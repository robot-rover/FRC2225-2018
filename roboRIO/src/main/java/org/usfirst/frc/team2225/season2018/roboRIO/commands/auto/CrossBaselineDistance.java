package org.usfirst.frc.team2225.season2018.roboRIO.commands.auto;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team2225.season2018.roboRIO.RoboRIOMain;
import org.usfirst.frc.team2225.season2018.roboRIO.Vector2D;

public class CrossBaselineDistance extends Command {
    public CrossBaselineDistance() {
        requires(RoboRIOMain.drivetrain);
    }

    long startTime;

    @Override
    protected void initialize() {
        RoboRIOMain.drivetrain.omniDistance(new Vector2D(0, 330));
        SmartDashboard.putNumber("Drivetrain Error", RoboRIOMain.drivetrain.getAverageError());
    }

    @Override
    protected void end() {
        RoboRIOMain.drivetrain.tankDrive(0, 0);
    }

    @Override
    protected boolean isFinished() {
        return RoboRIOMain.drivetrain.getAverageError() < PlaceCube.allowableClosedLoopError;
    }

}
