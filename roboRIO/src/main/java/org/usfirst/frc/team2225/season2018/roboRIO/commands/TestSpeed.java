package org.usfirst.frc.team2225.season2018.roboRIO.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.usfirst.frc.team2225.season2018.roboRIO.RoboRIOMain;

public class TestSpeed extends Command {
    public TestSpeed() {
        requires(RoboRIOMain.drivetrain);
    }

    @Override
    protected void initialize() {}

    @Override
    protected void execute() {
        super.execute();
    }

    @Override
    protected boolean isFinished() {
        return false;
    }
}
