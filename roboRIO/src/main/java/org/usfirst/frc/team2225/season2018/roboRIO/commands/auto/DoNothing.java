package org.usfirst.frc.team2225.season2018.roboRIO.commands.auto;

import edu.wpi.first.wpilibj.command.Command;
import org.usfirst.frc.team2225.season2018.roboRIO.RoboRIOMain;

public class DoNothing extends Command{
    public DoNothing() {
        requires(RoboRIOMain.drivetrain);
    }

    @Override
    protected boolean isFinished() {
        return false;
    }
}
