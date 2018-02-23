package org.usfirst.frc.team2225.season2018.roboRIO.commands.auto;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;
import org.usfirst.frc.team2225.season2018.roboRIO.RoboRIOMain;
import org.usfirst.frc.team2225.season2018.roboRIO.Vector2D;

public class CrossBaseline extends Command {
    boolean isDone;
    public CrossBaseline() {
        requires(RoboRIOMain.drivetrain);
        isDone = false;
    }

    long startTime;

    @Override
    protected void initialize() {
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void execute() {
        RoboRIOMain.drivetrain.tankDrive(0.4, 0.4);
    }

    @Override
    protected boolean isFinished() {
        return System.currentTimeMillis() - startTime > 1500;
    }

}