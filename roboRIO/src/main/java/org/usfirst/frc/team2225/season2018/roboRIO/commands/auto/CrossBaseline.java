package org.usfirst.frc.team2225.season2018.roboRIO.commands.auto;

import edu.wpi.first.wpilibj.command.Command;
import org.usfirst.frc.team2225.season2018.roboRIO.RoboRIOMain;
import org.usfirst.frc.team2225.season2018.roboRIO.Vector2D;

public class CrossBaseline extends Command {
    boolean isDone;
    public CrossBaseline() {
        requires(RoboRIOMain.drivetrain);
        isDone = false;
    }

    @Override
    protected void initialize() {
        RoboRIOMain.drivetrain.omniDistance(new Vector2D(0, 50));
    }

    @Override
    protected void execute() {
        isDone = RoboRIOMain.drivetrain.getAverageError() < RoboRIOMain.drivetrain.cmToCounts(4);
    }

    @Override
    protected boolean isFinished() {
        return isDone;
    }

}
